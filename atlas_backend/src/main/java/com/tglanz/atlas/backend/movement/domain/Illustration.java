package com.tglanz.atlas.backend.movement.domain;

/** An image of a Movement, found at its image URL. Order within an Avatar's list matters. */
public record Illustration(String imageUrl, Integer stagePosition) {
  public Illustration(String imageUrl) {
    this(imageUrl, null);
  }

  public Illustration {
    if (stagePosition != null && stagePosition < 0)
      throw new IllegalArgumentException("Stage position must not be negative");
    if (imageUrl == null || imageUrl.isBlank()) {
      throw new IllegalArgumentException("Illustration requires an image url");
    }
  }
}
