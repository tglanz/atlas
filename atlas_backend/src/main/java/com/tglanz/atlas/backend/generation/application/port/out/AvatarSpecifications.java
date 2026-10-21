package com.tglanz.atlas.backend.generation.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface AvatarSpecifications {
  Optional<String> find(UUID avatarId);
}
