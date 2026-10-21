package com.tglanz.atlas.backend.generation.adapter.out.avatar;

import com.tglanz.atlas.backend.avatar.application.port.in.AvatarUseCase;
import com.tglanz.atlas.backend.generation.application.port.out.AvatarSpecifications;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Reads Avatar specifications through the avatar module's public use case. */
@Component
class AvatarSpecificationsAdapter implements AvatarSpecifications {

  private final AvatarUseCase avatars;

  AvatarSpecificationsAdapter(AvatarUseCase avatars) {
    this.avatars = avatars;
  }

  @Override
  public Optional<String> find(UUID avatarId) {
    try {
      return Optional.of(avatars.get(avatarId).specification());
    } catch (NotFoundException e) {
      return Optional.empty();
    }
  }
}
