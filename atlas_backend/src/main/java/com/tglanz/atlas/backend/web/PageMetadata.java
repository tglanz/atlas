package com.tglanz.atlas.backend.web;

public record PageMetadata(
    String title,
    String description,
    String canonical,
    String robots,
    String image,
    String imageAlt,
    String structuredData) {
  public PageMetadata withRobots(String value) {
    return new PageMetadata(title, description, canonical, value, image, imageAlt, structuredData);
  }
}
