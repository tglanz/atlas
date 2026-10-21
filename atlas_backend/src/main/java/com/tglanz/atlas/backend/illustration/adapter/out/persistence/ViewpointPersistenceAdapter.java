package com.tglanz.atlas.backend.illustration.adapter.out.persistence;

import com.tglanz.atlas.backend.illustration.application.port.out.ViewpointRepository;
import com.tglanz.atlas.backend.illustration.domain.Viewpoint;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.persistence.ConstraintViolations;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
class ViewpointPersistenceAdapter implements ViewpointRepository {

  private final ViewpointJpaRepository jpa;

  ViewpointPersistenceAdapter(ViewpointJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public List<Viewpoint> findAll() {
    return jpa.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<Viewpoint> findById(UUID id) {
    return jpa.findById(id.toString()).map(this::toDomain);
  }

  @Override
  public void save(Viewpoint viewpoint) {
    var entity = new ViewpointEntity();
    entity.id = viewpoint.id().toString();
    entity.name = viewpoint.name();
    entity.specification = viewpoint.specification();
    try {
      jpa.saveAndFlush(entity);
    } catch (DataAccessException e) {
      if (!ConstraintViolations.isConstraintViolation(e)) {
        throw e;
      }
      throw new ConflictException("Viewpoint name already in use");
    }
  }

  @Override
  public void deleteById(UUID id) {
    jpa.deleteById(id.toString());
  }

  private Viewpoint toDomain(ViewpointEntity e) {
    return new Viewpoint(UUID.fromString(e.id), e.name, e.specification);
  }
}
