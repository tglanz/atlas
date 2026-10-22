package com.tglanz.atlas.backend.generation.domain;

/** Use an existing image as is. */
public record StaticImageSpec(String imageUrl) implements StageSpec {
  public StaticImageSpec {
    imageUrl = imageUrl == null ? "" : imageUrl.strip();
    if (imageUrl.isEmpty()) {
      throw new IllegalArgumentException("A static image needs an image url");
    }
  }
}
