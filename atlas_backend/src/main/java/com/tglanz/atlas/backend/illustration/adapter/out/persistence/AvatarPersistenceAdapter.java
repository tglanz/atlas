package com.tglanz.atlas.backend.illustration.adapter.out.persistence;

import com.tglanz.atlas.backend.illustration.application.port.out.AvatarRepository;
import com.tglanz.atlas.backend.illustration.domain.Avatar;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.persistence.ConstraintViolations;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
class AvatarPersistenceAdapter implements AvatarRepository {

  private final AvatarJpaRepository jpa;

  AvatarPersistenceAdapter(AvatarJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public List<Avatar> findAll() {
    return jpa.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<Avatar> findById(UUID id) {
    return jpa.findById(id.toString()).map(this::toDomain);
  }

  @Override
  public Optional<Avatar> findDefault() {
    return jpa.findFirstByIsDefaultTrue().map(this::toDomain);
  }

  @Override
  public void save(Avatar avatar) {
    var entity = new AvatarEntity();
    entity.id = avatar.id().toString();
    entity.name = avatar.name();
    entity.specification = avatar.specification();
    entity.isDefault = avatar.isDefault();
    try {
      jpa.saveAndFlush(entity);
    } catch (DataAccessException e) {
      if (!ConstraintViolations.isConstraintViolation(e)) {
        throw e;
      }
      throw new ConflictException("Avatar name already in use");
    }
  }

  @Override
  public void deleteById(UUID id) {
    try {
      jpa.deleteById(id.toString());
      jpa.flush();
    } catch (DataAccessException e) {
      if (!ConstraintViolations.isConstraintViolation(e)) {
        throw e;
      }
      throw new ConflictException("Avatar is still used by movement illustrations");
    }
  }

  private Avatar toDomain(AvatarEntity e) {
    return new Avatar(UUID.fromString(e.id), e.name, e.specification, e.isDefault);
  }
}
