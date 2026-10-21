package com.tglanz.atlas.backend.movement.application.port.out;

import com.tglanz.atlas.backend.movement.domain.AvatarChoice;

public interface AvatarSelector {
  /** The Avatars for a viewer; a null viewer (anonymous) has no preferred Avatar. */
  AvatarChoice choiceFor(String viewerId);
}
