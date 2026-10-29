package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sun.net.httpserver.HttpServer;
import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {"atlas.site-url=https://atlas.example", "atlas.preview.allowed-hosts=127.0.0.1"})
@AutoConfigureMockMvc
class MovementPreviewPagesTest {
  @Autowired MockMvc mvc;
  @Autowired MovementCommandUseCase movements;
  @Autowired PreviewImages previews;
  private HttpServer sourceServer;

  @BeforeEach
  void startSource() throws Exception {
    sourceServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    sourceServer.start();
  }

  @AfterEach
  void stopSource() {
    sourceServer.stop(0);
  }

  @Test
  void sharingUsesASmallCachedJpegWithoutCroppingOrChangingTheIllustration() throws Exception {
    AtomicInteger downloads = new AtomicInteger();
    byte[] original = png();
    String source = source("/first.png");
    sourceServer.createContext(
        "/first.png",
        exchange -> {
          downloads.incrementAndGet();
          exchange.getResponseHeaders().set("Content-Type", "image/png");
          exchange.sendResponseHeaders(200, original.length);
          try (var output = exchange.getResponseBody()) {
            output.write(original);
          }
        });
    String key = movement(source);
    String path = previews.path(key, source);
    for (String agent : List.of("WhatsApp/2.0", "facebookexternalhit/1.1")) {
      var result =
          mvc.perform(get("/movements/" + key).header("User-Agent", agent))
              .andExpect(status().isOk())
              .andReturn();
      String html = result.getResponse().getContentAsString();
      assertThat(html)
          .contains(
              "property=\"og:image\" content=\"https://atlas.example" + path + "\"",
              "property=\"og:image:type\" content=\"image/jpeg\"",
              "property=\"og:image:width\" content=\"512\"",
              "property=\"og:image:height\" content=\"512\"",
              "src=\"" + source + "\"");
    }
    assertThat(downloads).hasValue(0);
    byte[] jpeg =
        mvc.perform(get(path))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/jpeg"))
            .andExpect(header().string("Cache-Control", "max-age=31536000, public, immutable"))
            .andExpect(header().exists("ETag"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
    var thumbnail = ImageIO.read(new ByteArrayInputStream(jpeg));
    assertThat(thumbnail.getWidth()).isEqualTo(512);
    assertThat(thumbnail.getHeight()).isEqualTo(512);
    assertThat(new Color(thumbnail.getRGB(256, 20)).getRed()).isGreaterThan(245);
    assertThat(new Color(thumbnail.getRGB(256, 256)).getRed()).isGreaterThan(220);
    assertThat(new Color(thumbnail.getRGB(256, 256)).getGreen()).isLessThan(30);
    assertThat(jpeg.length).isLessThan(300_000);
    mvc.perform(get(path)).andExpect(status().isOk()).andExpect(content().bytes(jpeg));
    assertThat(downloads).hasValue(1);
    movements.replaceIllustrations(
        new MovementKey(key), List.of(new Illustration(source("/second.png"))));
    mvc.perform(get(path)).andExpect(status().isNotFound());
    assertThat(previews.path(key, source("/second.png"))).isNotEqualTo(path);
  }

  @Test
  void invalidImagesFailWithoutBeingCachedAndCanRecover() throws Exception {
    AtomicInteger attempts = new AtomicInteger();
    byte[] valid = png();
    String source = source("/retry.png");
    sourceServer.createContext(
        "/retry.png",
        exchange -> {
          byte[] bytes = attempts.incrementAndGet() == 1 ? new byte[] {1, 2, 3} : valid;
          exchange.sendResponseHeaders(200, bytes.length);
          try (var output = exchange.getResponseBody()) {
            output.write(bytes);
          }
        });
    String path = previews.path(movement(source), source);
    mvc.perform(get(path))
        .andExpect(status().isBadGateway())
        .andExpect(header().string("Cache-Control", "no-store"));
    mvc.perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType("image/jpeg"));
    assertThat(attempts).hasValue(2);
  }

  @Test
  void redirectsAndOversizedDownloadsAreNotFollowedOrDecoded() throws Exception {
    AtomicInteger target = new AtomicInteger();
    sourceServer.createContext(
        "/target.png",
        exchange -> {
          target.incrementAndGet();
          exchange.sendResponseHeaders(500, -1);
          exchange.close();
        });
    sourceServer.createContext(
        "/redirect.png",
        exchange -> {
          exchange.getResponseHeaders().set("Location", source("/target.png"));
          exchange.sendResponseHeaders(302, -1);
          exchange.close();
        });
    String redirect = source("/redirect.png");
    mvc.perform(get(previews.path(movement(redirect), redirect)))
        .andExpect(status().isBadGateway());
    assertThat(target).hasValue(0);
    sourceServer.createContext(
        "/huge.png",
        exchange -> {
          exchange.sendResponseHeaders(200, 9 * 1024 * 1024);
          exchange.close();
        });
    String huge = source("/huge.png");
    mvc.perform(get(previews.path(movement(huge), huge))).andExpect(status().isBadGateway());
  }

  @Test
  void unknownImagesAndUntrustedHostsDoNotCreateAnOpenProxy() throws Exception {
    var allowed = new PreviewImages("fal.media");
    assertThat(allowed.path("plank", "https://v3b.fal.media/files/plank.png")).isNotNull();
    for (String url :
        List.of(
            "https://evilfal.media/plank.png",
            "https://fal.media.evil.example/plank.png",
            "https://fal.media@127.0.0.1/plank.png",
            "file:///etc/passwd",
            "https://127.0.0.1/plank.png")) assertThat(allowed.path("plank", url)).isNull();
    String key = movement("https://untrusted.example/private.png");
    mvc.perform(
            get(
                "/movements/"
                    + key
                    + "/preview/"
                    + PreviewImages.version("https://untrusted.example/private.png")
                    + ".jpg"))
        .andExpect(status().isNotFound());
    mvc.perform(get("/movements/missing-preview-movement/preview/invalid.jpg"))
        .andExpect(status().isNotFound());
    String source = source("/unused.png");
    String validKey = movement(source);
    mvc.perform(get("/movements/" + validKey + "/preview/wrong-version.jpg"))
        .andExpect(status().isNotFound());
    movements.replaceIllustrations(new MovementKey(validKey), List.of());
    mvc.perform(get(previews.path(validKey, source))).andExpect(status().isNotFound());
  }

  private String movement(String source) {
    String key = "preview_" + UUID.randomUUID().toString().replace("-", "");
    movements.create(
        new MovementKey(key),
        new MovementContent("Preview movement", "Description", List.of(), List.of(), Set.of()));
    movements.replaceIllustrations(new MovementKey(key), List.of(new Illustration(source)));
    return key;
  }

  private String source(String path) {
    return "http://127.0.0.1:" + sourceServer.getAddress().getPort() + path;
  }

  private static byte[] png() throws Exception {
    var image = new BufferedImage(800, 400, BufferedImage.TYPE_INT_ARGB);
    var graphics = image.createGraphics();
    graphics.setColor(Color.RED);
    graphics.fillRect(0, 0, 800, 400);
    graphics.dispose();
    var bytes = new ByteArrayOutputStream();
    ImageIO.write(image, "png", bytes);
    return bytes.toByteArray();
  }
}
