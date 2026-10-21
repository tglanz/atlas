package com.tglanz.atlas.backend.movement.domain;

/** Directed link to another Movement, e.g. relationType "progression". */
public record MovementLink(MovementKey target, String relationType) {
  private static final int MAX_LENGTH = 64;

  public MovementLink {
    if (target == null) {
      throw new IllegalArgumentException("Link target is required");
    }
    if (relationType == null || relationType.isBlank() || relationType.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("Link relation type is required (max 64 characters)");
    }
    relationType = relationType.strip().toLowerCase();
  }
}
