package com.tglanz.atlas.backend.movement.domain;

/** A position or phase of a Movement that is worth showing, such as the bottom of a push-up. */
public record MovementStage(String name, String description) {
  public MovementStage {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Stage name is required");
    }
    if (description == null || description.isBlank()) {
      throw new IllegalArgumentException("Stage description is required");
    }
    name = name.strip();
    description = description.strip();
  }
}
