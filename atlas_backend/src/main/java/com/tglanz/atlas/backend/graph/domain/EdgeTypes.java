package com.tglanz.atlas.backend.graph.domain;

import java.util.List;

/**
 * The relationship types the system knows. Edges may carry any type, and unknown types are treated
 * as one-way; this list is what the user interface offers and where bidirectional types are
 * declared.
 */
public final class EdgeTypes {

  public static final List<EdgeType> ALL =
      List.of(
          new EdgeType("progression", "Progression", false),
          new EdgeType("regression", "Regression", false),
          new EdgeType("variant", "Variant", true));

  private EdgeTypes() {}

  public static boolean isBidirectional(String type) {
    return ALL.stream().anyMatch(t -> t.name().equals(type) && t.bidirectional());
  }
}
