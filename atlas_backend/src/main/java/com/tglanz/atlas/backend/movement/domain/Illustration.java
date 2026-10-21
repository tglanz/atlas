package com.tglanz.atlas.backend.movement.domain;

/** A picture of a Movement, found at url. Order within an Avatar's list matters. */
public record Illustration(String url) {
  public Illustration {
    if (url == null || url.isBlank()) {
      throw new IllegalArgumentException("Illustration requires a url");
    }
  }
}
