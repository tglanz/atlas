package com.tglanz.atlas.backend.account.application.port.out;

import java.util.UUID;

public interface AvatarCatalog {
  boolean exists(UUID avatarId);
}
