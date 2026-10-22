package com.tglanz.atlas.backend.account.adapter.out.persistence;

import com.tglanz.atlas.backend.account.application.port.out.UserRepository;
import com.tglanz.atlas.backend.account.domain.User;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.persistence.ConstraintViolations;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
class UserPersistenceAdapter implements UserRepository {

  private final UserJpaRepository jpa;

  UserPersistenceAdapter(UserJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public long count() {
    return jpa.count();
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpa.findByEmail(email).map(this::toDomain);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpa.findById(id.toString()).map(this::toDomain);
  }

  @Override
  public void save(User user) {
    var entity = new UserEntity();
    entity.id = user.id().toString();
    entity.email = user.email();
    entity.passwordHash = user.passwordHash();
    entity.role = user.role();
    entity.createdAt = user.createdAt();
    try {
      jpa.saveAndFlush(entity);
    } catch (DataAccessException e) {
      if (!ConstraintViolations.isConstraintViolation(e)) {
        throw e;
      }
      throw new ConflictException("Email already registered");
    }
  }

  private User toDomain(UserEntity e) {
    return new User(UUID.fromString(e.id), e.email, e.passwordHash, e.role, e.createdAt);
  }
}
