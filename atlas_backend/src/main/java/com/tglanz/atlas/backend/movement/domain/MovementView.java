package com.tglanz.atlas.backend.movement.domain;

import java.util.List;
import java.util.UUID;

/** A Movement revision as shown to a viewer: Illustrations of one Avatar only. */
public record MovementView(Movement movement, UUID avatarId, List<Illustration> illustrations) {

  public static MovementView of(Movement movement, AvatarChoice choice) {
    UUID avatarId = choice.resolve(movement.illustrations());
    List<Illustration> illustrations =
        avatarId == null ? List.of() : movement.illustrations().get(avatarId);
    return new MovementView(movement, avatarId, illustrations);
  }
}
