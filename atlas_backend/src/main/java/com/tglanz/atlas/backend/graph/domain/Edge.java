package com.tglanz.atlas.backend.graph.domain;

/** A directed, typed relationship between two Movements, for example push-up progression to ... */
public record Edge(String source, String target, String type) {
  private static final int MAX_TYPE_LENGTH = 64;

  public Edge {
    if (source == null || source.isBlank() || target == null || target.isBlank()) {
      throw new IllegalArgumentException("An edge needs a source and a target");
    }
    if (source.equals(target)) {
      throw new IllegalArgumentException("A movement cannot be related to itself");
    }
    if (type == null || type.isBlank() || type.length() > MAX_TYPE_LENGTH) {
      throw new IllegalArgumentException("An edge type is required (max 64 characters)");
    }
    type = type.strip().toLowerCase();
  }

  /** The relationship seen from the other end, with the type that end gets. */
  public Edge reversedAs(String otherType) {
    return new Edge(target, source, otherType);
  }
}
