package com.tglanz.atlas.backend.generation.domain;

/** One stage of a movement to illustrate, described in words, by a reference picture, or both. */
public record Stage(String description, String referenceUrl) {
  public Stage {
    description = description == null ? "" : description.strip();
    referenceUrl = referenceUrl == null ? "" : referenceUrl.strip();
    if (description.isEmpty() && referenceUrl.isEmpty()) {
      throw new IllegalArgumentException("A stage needs a description or a reference picture");
    }
  }
}
