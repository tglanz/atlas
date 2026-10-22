package com.tglanz.atlas.backend.account.adapter.in.bootstrap;

import com.tglanz.atlas.backend.account.application.port.in.AccountUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the root maintainer from configuration when the application starts, if no user with that
 * email exists yet. Nothing happens when the email or password is not configured. The password is
 * never logged.
 */
@Component
class RootUserBootstrap implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(RootUserBootstrap.class);

  private final AccountUseCase accounts;
  private final String email;
  private final String password;

  RootUserBootstrap(
      AccountUseCase accounts,
      @Value("${atlas.root-user.email:}") String email,
      @Value("${atlas.root-user.password:}") String password) {
    this.accounts = accounts;
    this.email = email;
    this.password = password;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (email.isBlank() || password.isBlank()) {
      log.info("Root user is not configured; skipping");
      return;
    }
    try {
      boolean created = accounts.ensureMaintainer(email, password);
      log.info(created ? "Created the root user" : "The root user already exists");
    } catch (IllegalArgumentException e) {
      // Do not stop the application over a bad value, but do not stay silent about it.
      log.warn("Could not create the root user: {}", e.getMessage());
    }
  }
}
