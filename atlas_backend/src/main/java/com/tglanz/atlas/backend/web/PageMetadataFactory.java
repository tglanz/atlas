package com.tglanz.atlas.backend.web;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Builds metadata from a trusted deployment origin, never from the incoming Host header. */
@Component
public class PageMetadataFactory {
  private static final String BRAND = "Form Atlas";
  private final String siteUrl;
  private final JsonMapper json = JsonMapper.builder().build();

  public PageMetadataFactory(@Value("${atlas.site-url:http://localhost:8080}") String siteUrl) {
    URI origin = URI.create(siteUrl.strip());
    if (!("http".equals(origin.getScheme()) || "https".equals(origin.getScheme()))
        || origin.getHost() == null
        || origin.getUserInfo() != null
        || origin.getQuery() != null
        || origin.getFragment() != null
        || !(origin.getPath().isEmpty() || origin.getPath().equals("/"))) {
      throw new IllegalArgumentException("atlas.site-url must be an absolute HTTP(S) origin");
    }
    this.siteUrl = origin.toString().replaceAll("/+$", "");
  }

  public String absolute(String path) {
    if (path == null
        || !path.startsWith("/")
        || path.startsWith("//")
        || path.contains("\\")
        || path.contains("\r")
        || path.contains("\n")) {
      throw new IllegalArgumentException("A local absolute path is required");
    }
    return siteUrl + path;
  }

  public PageMetadata publicPage(String title, String description, String path, String image) {
    String canonical = absolute(path);
    String summary = summarize(description);
    String picture = imageUrl(image);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("@context", "https://schema.org");
    data.put("@type", "WebPage");
    data.put("name", title);
    data.put("description", summary);
    data.put("url", canonical);
    data.put("isPartOf", Map.of("@type", "WebSite", "name", BRAND, "url", absolute("/")));
    if (picture != null) data.put("image", picture);
    // JSON is inserted in a script element. Escape HTML delimiters even inside JSON strings.
    String structured =
        json.writeValueAsString(data)
            .replace("<", "\\u003c")
            .replace(">", "\\u003e")
            .replace("&", "\\u0026")
            .replace("\u2028", "\\u2028")
            .replace("\u2029", "\\u2029");
    return new PageMetadata(
        branded(title), summary, canonical, "index,follow", picture, structured);
  }

  public PageMetadata privatePage(String title) {
    return new PageMetadata(branded(title), "", null, "noindex,nofollow", null, null);
  }

  private static String branded(String title) {
    return BRAND.equals(title) ? BRAND : title + " | " + BRAND;
  }

  private static String summarize(String text) {
    String value = text == null ? "" : text.replaceAll("\\s+", " ").strip();
    if (value.length() <= 160) return value;
    int end = value.lastIndexOf(' ', 157);
    return value.substring(0, end > 100 ? end : 157) + "…";
  }

  private String imageUrl(String image) {
    if (image == null || image.isBlank()) return null;
    try {
      if (image.startsWith("/") && !image.startsWith("//")) return absolute(image);
      URI uri = URI.create(image);
      return (("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
              && uri.getHost() != null
              && uri.getUserInfo() == null)
          ? uri.toASCIIString()
          : null;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
