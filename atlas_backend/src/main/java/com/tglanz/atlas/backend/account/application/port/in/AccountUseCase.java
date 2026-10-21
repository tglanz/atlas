package com.tglanz.atlas.backend.account.application.port.in;

import com.tglanz.atlas.backend.account.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface AccountUseCase {

  /** Registers a user. The first registered user becomes a maintainer. */
  User register(String email, String rawPassword);

  Optional<User> findByEmail(String email);

  User get(UUID id);

  /** Sets the Avatar whose illustrations the user wants to see; null clears the preference. */
  User setPreferredAvatar(UUID userId, UUID avatarId);
}
