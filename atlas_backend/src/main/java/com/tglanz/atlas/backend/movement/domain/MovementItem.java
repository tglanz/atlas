package com.tglanz.atlas.backend.movement.domain;

/** A piece of text about a Movement, such as a performance cue or a comment. */
public record MovementItem(ItemType type, String content) {
  public MovementItem {
    if (type == null) {
      throw new IllegalArgumentException("Item type is required");
    }
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Item content is required");
    }
    content = content.strip();
  }
}
