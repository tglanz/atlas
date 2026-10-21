package com.tglanz.atlas.backend.movement.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** The Avatars a viewer wants illustrations for: their preferred one, then the default. */
public record AvatarChoice(UUID preferred, UUID fallback) {

  /** The first of the preferred and fallback Avatars that has Illustrations, or null. */
  public UUID resolve(Map<UUID, List<Illustration>> available) {
    for (UUID candidate : new UUID[] {preferred, fallback}) {
      if (candidate != null && !available.getOrDefault(candidate, List.of()).isEmpty()) {
        return candidate;
      }
    }
    return null;
  }
}
