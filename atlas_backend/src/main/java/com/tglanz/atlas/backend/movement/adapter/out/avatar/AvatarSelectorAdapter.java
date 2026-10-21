package com.tglanz.atlas.backend.movement.adapter.out.avatar;

import com.tglanz.atlas.backend.account.application.port.in.AccountUseCase;
import com.tglanz.atlas.backend.avatar.application.port.in.AvatarUseCase;
import com.tglanz.atlas.backend.avatar.domain.Avatar;
import com.tglanz.atlas.backend.movement.application.port.out.AvatarSelector;
import com.tglanz.atlas.backend.movement.application.port.out.KnownAvatars;
import com.tglanz.atlas.backend.movement.domain.AvatarChoice;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Reads the viewer's preference and the default Avatar through other features' use cases. */
@Component
class AvatarSelectorAdapter implements AvatarSelector, KnownAvatars {

  private final AccountUseCase accounts;
  private final AvatarUseCase avatars;

  AvatarSelectorAdapter(AccountUseCase accounts, AvatarUseCase avatars) {
    this.accounts = accounts;
    this.avatars = avatars;
  }

  @Override
  public AvatarChoice choiceFor(String viewerId) {
    UUID fallback = avatars.findDefault().map(Avatar::id).orElse(null);
    return new AvatarChoice(preferredOf(viewerId), fallback);
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

  private UUID preferredOf(String viewerId) {
    if (viewerId == null) {
      return null;
    }
    try {
      return accounts.get(UUID.fromString(viewerId)).preferredAvatarId();
    } catch (IllegalArgumentException | NotFoundException e) {
      return null;
    }
  }
}
