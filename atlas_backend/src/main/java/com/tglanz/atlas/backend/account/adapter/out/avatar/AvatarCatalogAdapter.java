package com.tglanz.atlas.backend.account.adapter.out.avatar;

import com.tglanz.atlas.backend.account.application.port.out.AvatarCatalog;
import com.tglanz.atlas.backend.avatar.application.port.in.AvatarUseCase;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Checks Avatars through the illustration module's public use case. */
@Component
class AvatarCatalogAdapter implements AvatarCatalog {

  private final AvatarUseCase avatars;

  AvatarCatalogAdapter(AvatarUseCase avatars) {
    this.avatars = avatars;
  }

  @Override
  public boolean exists(UUID avatarId) {
    try {
      avatars.get(avatarId);
      return true;
    } catch (NotFoundException e) {
      return false;
    }
  }
}
