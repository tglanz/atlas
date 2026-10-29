package com.tglanz.atlas.backend.web;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Small, reusable sharing images. Page rendering never downloads or decodes an illustration. */
@Component
class PreviewImages {
  static final int SIZE = 512;
  private static final int MAX_SOURCE_BYTES = 8 * 1024 * 1024;
  private static final long MAX_SOURCE_PIXELS = 16_000_000;
  private static final int CACHE_SIZE = 64;

  private final Set<String> allowedHosts;
  private final Map<String, byte[]> cache = new LinkedHashMap<>(CACHE_SIZE, .75f, true);

  PreviewImages(@Value("${atlas.preview.allowed-hosts:fal.media}") String allowedHosts) {
    this.allowedHosts =
        Arrays.stream(allowedHosts.split(","))
            .map(String::strip)
            .filter(host -> !host.isEmpty())
            .map(host -> host.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
  }

  String path(String key, String source) {
    return allowed(source) ? "/movements/" + key + "/preview/" + version(source) + ".jpg" : null;
  }

  static String version(String source) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(("jpeg-v1:" + source).getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest, 0, 12);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private boolean allowed(String source) {
    if (source == null) return false;
    try {
      URI uri = URI.create(source);
      if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
          || uri.getHost() == null
          || uri.getUserInfo() != null
          || uri.getFragment() != null) return false;
      String host = uri.getHost().toLowerCase(Locale.ROOT);
      return allowedHosts.stream()
          .anyMatch(allowed -> host.equals(allowed) || host.endsWith("." + allowed));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  byte[] jpeg(String source) throws IOException {
    if (!allowed(source)) throw new IOException("Image host is not allowed for previews");
    synchronized (cache) {
      byte[] existing = cache.get(source);
      if (existing != null) return existing;
    }
    byte[] rendered = render(download(URI.create(source)));
    synchronized (cache) {
      cache.put(source, rendered);
      if (cache.size() > CACHE_SIZE) cache.remove(cache.keySet().iterator().next());
    }
    return rendered;
  }

  private static byte[] download(URI source) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) source.toURL().openConnection();
    connection.setConnectTimeout(2000);
    connection.setReadTimeout(5000);
    // A stored image URL must not redirect this public endpoint to another host.
    connection.setInstanceFollowRedirects(false);
    try {
      if (connection.getResponseCode() != 200)
        throw new IOException("Image host did not return an image");
      if (connection.getContentLengthLong() > MAX_SOURCE_BYTES)
        throw new IOException("Source image is too large");
      try (var input = connection.getInputStream()) {
        byte[] bytes = input.readNBytes(MAX_SOURCE_BYTES + 1);
        if (bytes.length > MAX_SOURCE_BYTES) throw new IOException("Source image is too large");
        return bytes;
      }
    } finally {
      connection.disconnect();
    }
  }

  private static byte[] render(byte[] source) throws IOException {
    BufferedImage original;
    try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(source))) {
      var readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) throw new IOException("Unsupported image format");
      var reader = readers.next();
      try {
        reader.setInput(input, true, true);
        int width = reader.getWidth(0);
        int height = reader.getHeight(0);
        if (width <= 0 || height <= 0 || (long) width * height > MAX_SOURCE_PIXELS)
          throw new IOException("Source image dimensions are too large");
        original = reader.read(0);
      } finally {
        reader.dispose();
      }
    }
    BufferedImage thumbnail = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
    var graphics = thumbnail.createGraphics();
    try {
      graphics.setColor(Color.WHITE);
      graphics.fillRect(0, 0, SIZE, SIZE);
      graphics.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      double scale =
          Math.min((double) SIZE / original.getWidth(), (double) SIZE / original.getHeight());
      int width = Math.max(1, (int) Math.round(original.getWidth() * scale));
      int height = Math.max(1, (int) Math.round(original.getHeight() * scale));
      graphics.drawImage(original, (SIZE - width) / 2, (SIZE - height) / 2, width, height, null);
    } finally {
      graphics.dispose();
      original.flush();
    }
    var bytes = new ByteArrayOutputStream();
    var writer = ImageIO.getImageWritersByFormatName("jpeg").next();
    try (var output = new MemoryCacheImageOutputStream(bytes)) {
      writer.setOutput(output);
      var parameters = writer.getDefaultWriteParam();
      parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      parameters.setCompressionQuality(.8f);
      writer.write(null, new IIOImage(thumbnail, null, null), parameters);
    } finally {
      writer.dispose();
      thumbnail.flush();
    }
    return bytes.toByteArray();
  }
}
