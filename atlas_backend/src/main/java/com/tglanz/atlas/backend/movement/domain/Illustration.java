package com.tglanz.atlas.backend.movement.domain;

/** An image of a Movement, found at its image URL. Order within an Avatar's list matters. */
public record Illustration(String imageUrl) {
  public Illustration {
    if (imageUrl == null || imageUrl.isBlank()) {
      throw new IllegalArgumentException("Illustration requires an image url");
    }
  }
}
