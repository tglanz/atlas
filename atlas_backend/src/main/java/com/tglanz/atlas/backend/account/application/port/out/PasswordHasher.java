package com.tglanz.atlas.backend.account.application.port.out;

public interface PasswordHasher {
  String hash(String rawPassword);
}
