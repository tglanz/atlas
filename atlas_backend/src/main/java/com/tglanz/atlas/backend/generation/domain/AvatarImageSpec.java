package com.tglanz.atlas.backend.generation.domain;

import java.util.UUID;

/** Generate the picture with an Avatar and a Viewpoint, from instructions and/or a reference. */
public record AvatarImageSpec(
    UUID avatarId, UUID viewpointId, String instructions, String referenceImageUrl)
    implements StageSpec {
  public AvatarImageSpec {
    if (avatarId == null || viewpointId == null) {
      throw new IllegalArgumentException("An avatar and a viewpoint are required");
    }
    instructions = instructions == null ? "" : instructions.strip();
    referenceImageUrl = referenceImageUrl == null ? "" : referenceImageUrl.strip();
    if (instructions.isEmpty() && referenceImageUrl.isEmpty()) {
      throw new IllegalArgumentException("Give instructions, a reference image, or both");
    }
  }
}
