package com.tglanz.atlas.backend.account.application.port.out;

import com.tglanz.atlas.backend.account.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
  long count();

  Optional<User> findByEmail(String email);

  Optional<User> findById(UUID id);

  /** Throws ConflictException when the email is taken. */
  void save(User user);
}
