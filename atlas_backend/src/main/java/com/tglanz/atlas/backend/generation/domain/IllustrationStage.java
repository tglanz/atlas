package com.tglanz.atlas.backend.generation.domain;

import java.util.UUID;

/** One image to generate: an Avatar, a Viewpoint and a Movement stage, plus its candidate. */
public record IllustrationStage(
    int position,
    UUID avatarId,
    UUID viewpointId,
    String name,
    String description,
    IllustrationCandidate candidate) {

  public IllustrationStage {
    if (avatarId == null || viewpointId == null || candidate == null) {
      throw new IllegalArgumentException("A stage needs an avatar, a viewpoint and a candidate");
    }
    if (name == null || name.isBlank() || description == null || description.isBlank()) {
      throw new IllegalArgumentException("A stage needs a name and a description");
    }
    name = name.strip();
    description = description.strip();
  }

  public IllustrationStage withCandidate(IllustrationCandidate newCandidate) {
    return new IllustrationStage(position, avatarId, viewpointId, name, description, newCandidate);
  }
}
