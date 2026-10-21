package com.tglanz.atlas.backend.routine.domain;

/** Reference to an exact published revision of a Movement. */
public record MovementRef(String key, int revision) {
  public MovementRef {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Movement key is required");
    }
    if (revision < 1) {
      throw new IllegalArgumentException("Movement revision starts at 1");
    }
  }
}
