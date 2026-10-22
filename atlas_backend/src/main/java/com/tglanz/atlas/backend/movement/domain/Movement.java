package com.tglanz.atlas.backend.movement.domain;

import java.util.List;

/** A Movement. Maintainers edit it in place. */
public final class Movement {
  private final MovementKey key;
  private MovementContent content;
  private List<Illustration> illustrations;

  private Movement(MovementKey key, MovementContent content, List<Illustration> illustrations) {
    if (key == null || content == null) {
      throw new IllegalArgumentException("Key and content are required");
    }
    this.key = key;
    this.content = content;
    this.illustrations = List.copyOf(illustrations);
  }

  public static Movement create(MovementKey key, MovementContent content) {
    return new Movement(key, content, List.of());
  }

  /** Rebuilds a stored Movement. */
  public static Movement restore(
      MovementKey key, MovementContent content, List<Illustration> illustrations) {
    return new Movement(key, content, illustrations);
  }

  public void edit(MovementContent newContent) {
    if (newContent == null) {
      throw new IllegalArgumentException("Content is required");
    }
    this.content = newContent;
  }

  /** Replaces the whole sequence; an empty list removes all Illustrations. */
  public void replaceIllustrations(List<Illustration> sequence) {
    if (sequence == null) {
      throw new IllegalArgumentException("Illustrations are required");
    }
    this.illustrations = List.copyOf(sequence);
  }

  public MovementKey key() {
    return key;
  }

  public MovementContent content() {
    return content;
  }

  /** The ordered Illustrations. */
  public List<Illustration> illustrations() {
    return illustrations;
  }
}
