package com.tglanz.atlas.backend.movement.domain;

/** A picture of a Movement, stored under objectKey. Order within an Avatar's list matters. */
public record Illustration(String objectKey) {
  public Illustration {
    if (objectKey == null || objectKey.isBlank()) {
      throw new IllegalArgumentException("Illustration requires an object key");
    }
  }
}
