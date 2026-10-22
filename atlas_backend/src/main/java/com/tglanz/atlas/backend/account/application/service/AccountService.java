package com.tglanz.atlas.backend.account.application.service;

import com.tglanz.atlas.backend.account.application.port.in.AccountUseCase;
import com.tglanz.atlas.backend.account.application.port.out.PasswordHasher;
import com.tglanz.atlas.backend.account.application.port.out.UserRepository;
import com.tglanz.atlas.backend.account.domain.Role;
import com.tglanz.atlas.backend.account.domain.User;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountService implements AccountUseCase {

  static final int MIN_PASSWORD_LENGTH = 8;
  // bcrypt ignores input beyond 72 bytes
  static final int MAX_PASSWORD_LENGTH = 72;

  private final UserRepository users;
  private final PasswordHasher hasher;
  private final Clock clock;

  public AccountService(UserRepository users, PasswordHasher hasher, Clock clock) {
    this.users = users;
    this.hasher = hasher;
    this.clock = clock;
  }

  @Override
  public User register(String email, String rawPassword) {
    String normalized = User.normalizeEmail(email);
    requireAcceptablePassword(rawPassword);
    if (users.findByEmail(normalized).isPresent()) {
      throw new ConflictException("Email already registered");
    }
    Role role = users.count() == 0 ? Role.MAINTAINER : Role.PRACTITIONER;
    return create(normalized, rawPassword, role);
  }

  @Override
  public boolean ensureMaintainer(String email, String rawPassword) {
    String normalized = User.normalizeEmail(email);
    if (users.findByEmail(normalized).isPresent()) {
      return false;
    }
    requireAcceptablePassword(rawPassword);
    create(normalized, rawPassword, Role.MAINTAINER);
    return true;
  }

  private User create(String normalizedEmail, String rawPassword, Role role) {
    User user =
        new User(
            UUID.randomUUID(), normalizedEmail, hasher.hash(rawPassword), role, clock.instant());
    users.save(user);
    return user;
  }

  private static void requireAcceptablePassword(String rawPassword) {
    if (rawPassword == null
        || rawPassword.length() < MIN_PASSWORD_LENGTH
        || rawPassword.length() > MAX_PASSWORD_LENGTH) {
      throw new IllegalArgumentException(
          "Password must be %d to %d characters"
              .formatted(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    try {
      return users.findByEmail(User.normalizeEmail(email));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  @Override
  @Transactional(readOnly = true)
  public User get(UUID id) {
    return users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
  }
}
