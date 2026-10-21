package com.tglanz.atlas.backend.movement.application.port.out;

import java.util.UUID;

public interface KnownAvatars {
  boolean exists(UUID avatarId);
}
