package com.tglanz.atlas.backend.account.domain;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

public record User(
    UUID id,
    String email,
    String passwordHash,
    Role role,
    UUID preferredAvatarId,
    Instant createdAt) {
  private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
  private static final int MAX_EMAIL_LENGTH = 255;

  public User {
    if (id == null || role == null || createdAt == null) {
      throw new IllegalArgumentException("Id, role and creation time are required");
    }
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new IllegalArgumentException("Password hash is required");
    }
    email = normalizeEmail(email);
  }

  public static String normalizeEmail(String email) {
    String normalized = email == null ? "" : email.strip().toLowerCase();
    if (normalized.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(normalized).matches()) {
      throw new IllegalArgumentException("A valid email address is required");
    }
    return normalized;
  }

  public User withPreferredAvatar(UUID avatarId) {
    return new User(id, email, passwordHash, role, avatarId, createdAt);
  }

  public boolean isMaintainer() {
    return role == Role.MAINTAINER;
  }
}
