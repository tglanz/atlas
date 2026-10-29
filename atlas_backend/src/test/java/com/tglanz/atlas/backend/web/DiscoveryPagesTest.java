package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@SpringBootTest(properties = "atlas.site-url=https://atlas.example")
@AutoConfigureMockMvc
class DiscoveryPagesTest {
  @Autowired MockMvc mvc;
  @Autowired MovementQueryUseCase movements;
  @Autowired MovementCommandUseCase commands;
  private static final String SITEMAP = "http://www.sitemaps.org/schemas/sitemap/0.9";
  private static final String IMAGES = "http://www.google.com/schemas/sitemap-image/1.1";

  private Document sitemap() throws Exception {
    String xml =
        mvc.perform(get("/sitemap.xml").header("Host", "untrusted.example"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    var factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
  }

  @Test
  void sitemapContainsEveryMovementAtItsTrustedCanonicalUrl() throws Exception {
    var expected =
        new ArrayList<>(List.of("https://atlas.example/", "https://atlas.example/movements"));
    movements
        .names()
        .forEach(
            movement -> expected.add("https://atlas.example/movements/" + movement.key().value()));
    var locations = sitemap().getElementsByTagNameNS(SITEMAP, "loc");
    var actual = new ArrayList<String>();
    for (int index = 0; index < locations.getLength(); index++)
      actual.add(locations.item(index).getTextContent());
    assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    assertThat(actual)
        .noneMatch(url -> url.contains("/routines") || url.contains("untrusted.example"));
  }

  @Test
  void imageEntriesUseTheFirstIllustrationAndEscapeItsUrlCorrectly() throws Exception {
    String key = "discovery_" + UUID.randomUUID().toString().replace("-", "");
    commands.create(
        new MovementKey(key),
        new MovementContent("Discovery example", "Description", List.of(), List.of(), Set.of()));
    commands.replaceIllustrations(
        new MovementKey(key),
        List.of(
            new Illustration("/images/first.png?view=side&size=1024"),
            new Illustration("/images/second.png")));
    NodeList urls = sitemap().getElementsByTagNameNS(SITEMAP, "url");
    Element match = null;
    for (int index = 0; index < urls.getLength(); index++) {
      Element entry = (Element) urls.item(index);
      if (entry
          .getElementsByTagNameNS(SITEMAP, "loc")
          .item(0)
          .getTextContent()
          .equals("https://atlas.example/movements/" + key)) match = entry;
    }
    assertThat(match).isNotNull();
    NodeList images = match.getElementsByTagNameNS(IMAGES, "loc");
    assertThat(images.getLength()).isEqualTo(1);
    assertThat(images.item(0).getTextContent())
        .isEqualTo("https://atlas.example/images/first.png?view=side&size=1024");
  }

  @Test
  void robotsAdvertisesTheSitemapAndAllowsSearchCrawlers() throws Exception {
    String robots =
        mvc.perform(get("/robots.txt").header("User-Agent", "OAI-SearchBot/1.4"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(robots)
        .contains(
            "User-agent: *",
            "User-agent: OAI-SearchBot",
            "Allow: /\n",
            "Allow: /routines/shared/",
            "Sitemap: https://atlas.example/sitemap.xml")
        .doesNotContain("Disallow: /\n", "localhost");
  }
}
