package com.tglanz.atlas.backend.account.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tglanz.atlas.backend.account.application.port.out.UserRepository;
import com.tglanz.atlas.backend.account.application.service.AccountService;
import com.tglanz.atlas.backend.account.domain.Role;
import com.tglanz.atlas.backend.account.domain.User;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountServiceTest {

  private static final class FakeUsers implements UserRepository {
    final Map<String, User> byEmail = new HashMap<>();

    public long count() {
      return byEmail.size();
    }

    public Optional<User> findByEmail(String email) {
      return Optional.ofNullable(byEmail.get(email));
    }

    public Optional<User> findById(UUID id) {
      return byEmail.values().stream().filter(u -> u.id().equals(id)).findFirst();
    }

    public void save(User user) {
      byEmail.put(user.email(), user);
    }
  }

  private final FakeUsers users = new FakeUsers();
  private final AccountService accounts =
      new AccountService(users, raw -> "hash:" + raw, Clock.systemUTC());

  @Test
  void createsTheRootMaintainerWhenNoUserHasTheEmail() {
    assertThat(accounts.ensureMaintainer("Root@Example.com", "long-enough")).isTrue();
    User user = users.byEmail.get("root@example.com");
    assertThat(user.role()).isEqualTo(Role.MAINTAINER);
    assertThat(user.passwordHash()).isEqualTo("hash:long-enough");
  }

  @Test
  void leavesAnExistingUserAsItIs() {
    accounts.register("first@example.com", "first-password");
    accounts.register("root@example.com", "chosen-password");

    assertThat(accounts.ensureMaintainer("root@example.com", "other-password")).isFalse();

    User root = users.byEmail.get("root@example.com");
    assertThat(root.role()).isEqualTo(Role.PRACTITIONER);
    assertThat(root.passwordHash()).isEqualTo("hash:chosen-password");
  }

  @Test
  void runningTwiceCreatesOnlyOne() {
    assertThat(accounts.ensureMaintainer("root@example.com", "long-enough")).isTrue();
    assertThat(accounts.ensureMaintainer("root@example.com", "long-enough")).isFalse();
    assertThat(users.count()).isEqualTo(1);
  }

  @Test
  void rejectsAnUnacceptablePasswordOrEmail() {
    assertThatThrownBy(() -> accounts.ensureMaintainer("root@example.com", "short"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> accounts.ensureMaintainer("not-an-email", "long-enough"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(users.count()).isZero();
  }
}
