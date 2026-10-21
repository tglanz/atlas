package com.tglanz.atlas.backend.account.adapter.out.security;

import com.tglanz.atlas.backend.account.application.port.out.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class SpringPasswordHasher implements PasswordHasher {

  private final PasswordEncoder encoder;

  SpringPasswordHasher(PasswordEncoder encoder) {
    this.encoder = encoder;
  }

  @Override
  public String hash(String rawPassword) {
    return encoder.encode(rawPassword);
  }
}
