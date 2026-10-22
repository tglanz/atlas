package com.tglanz.atlas.backend.account.application.port.in;

import com.tglanz.atlas.backend.account.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface AccountUseCase {

  /** Registers a user. The first registered user becomes a maintainer. */
  User register(String email, String rawPassword);

  /**
   * Makes sure a maintainer with this email exists. Creates it if there is no user with the email.
   * An existing user is left as it is, including its password and role. Returns whether a user was
   * created.
   */
  boolean ensureMaintainer(String email, String rawPassword);

  Optional<User> findByEmail(String email);

  User get(UUID id);
}
