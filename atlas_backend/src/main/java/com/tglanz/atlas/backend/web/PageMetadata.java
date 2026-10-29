package com.tglanz.atlas.backend.web;

public record PageMetadata(
    String title,
    String description,
    String canonical,
    String robots,
    String image,
    String imageAlt,
    String structuredData,
    String imageType,
    Integer imageWidth,
    Integer imageHeight) {
  public PageMetadata withImageDetails(String type, int width, int height) {
    return new PageMetadata(
        title,
        description,
        canonical,
        robots,
        image,
        imageAlt,
        structuredData,
        type,
        width,
        height);
  }

  public PageMetadata withRobots(String value) {
    return new PageMetadata(
        title,
        description,
        canonical,
        value,
        image,
        imageAlt,
        structuredData,
        imageType,
        imageWidth,
        imageHeight);
  }
}
