package com.tglanz.atlas.backend.account.adapter.in.security;

import com.tglanz.atlas.backend.account.application.port.in.AccountUseCase;
import com.tglanz.atlas.backend.account.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Logs users in by email. The authenticated principal's name is the user id, so ownership does not
 * change if the email does.
 */
@Component
class AccountUserDetailsService implements UserDetailsService {

  private final AccountUseCase accounts;

  AccountUserDetailsService(AccountUseCase accounts) {
    this.accounts = accounts;
  }

  @Override
  public UserDetails loadUserByUsername(String email) {
    User user =
        accounts
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
    return org.springframework.security.core.userdetails.User.withUsername(user.id().toString())
        .password(user.passwordHash())
        .roles(user.role().name())
        .build();
  }
}
