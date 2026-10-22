package com.tglanz.atlas.backend.generation.domain;

import java.util.UUID;

/** One image to generate: an Avatar, a Viewpoint and instructions, plus its candidate. */
public record IllustrationStage(
    int position,
    UUID avatarId,
    UUID viewpointId,
    String instructions,
    IllustrationCandidate candidate) {

  public IllustrationStage {
    if (avatarId == null || viewpointId == null || candidate == null) {
      throw new IllegalArgumentException("A stage needs an avatar, a viewpoint and a candidate");
    }
    if (instructions == null || instructions.isBlank()) {
      throw new IllegalArgumentException("A stage needs instructions");
    }
    instructions = instructions.strip();
  }

  public IllustrationStage withCandidate(IllustrationCandidate newCandidate) {
    return new IllustrationStage(position, avatarId, viewpointId, instructions, newCandidate);
  }
}
