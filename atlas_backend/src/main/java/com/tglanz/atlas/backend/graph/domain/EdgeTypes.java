package com.tglanz.atlas.backend.graph.domain;

import java.util.List;
import java.util.Optional;

/**
 * The relationship types the system knows. Edges may carry any type, and unknown types are treated
 * as one-way; this list is what the user interface offers and where inverses are declared.
 */
public final class EdgeTypes {

  public static final List<EdgeType> ALL =
      List.of(
          new EdgeType("progression", "Progression", "Progressions", "regression"),
          new EdgeType("regression", "Regression", "Regressions", "progression"),
          new EdgeType("variant", "Variant", "Variants", "variant"));

  private EdgeTypes() {}

  /** The type the other movement gets when an edge of this type is added, if the type has one. */
  public static Optional<String> inverseOf(String type) {
    return ALL.stream()
        .filter(t -> t.name().equals(type))
        .map(EdgeType::inverse)
        .filter(inverse -> inverse != null)
        .findFirst();
  }
}
