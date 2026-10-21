package com.tglanz.atlas.backend.routine.application.service;

import com.tglanz.atlas.backend.routine.application.port.in.RoutineUseCase;
import com.tglanz.atlas.backend.routine.application.port.out.PublishedMovementCatalog;
import com.tglanz.atlas.backend.routine.application.port.out.RoutineRepository;
import com.tglanz.atlas.backend.routine.domain.MovementRef;
import com.tglanz.atlas.backend.routine.domain.Routine;
import com.tglanz.atlas.backend.routine.domain.RoutineContent;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoutineService implements RoutineUseCase {

  private final RoutineRepository repository;
  private final PublishedMovementCatalog catalog;

  public RoutineService(RoutineRepository repository, PublishedMovementCatalog catalog) {
    this.repository = repository;
    this.catalog = catalog;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Routine> list(String ownerId) {
    return repository.findByOwner(ownerId);
  }

  @Override
  @Transactional(readOnly = true)
  public Routine get(String ownerId, UUID id) {
    return repository
        .findById(id)
        .filter(r -> r.isOwnedBy(ownerId))
        .orElseThrow(() -> new NotFoundException("Routine not found"));
  }

  @Override
  public Routine create(String ownerId, RoutineContent content) {
    requirePublished(content);
    Routine routine = Routine.create(ownerId, content);
    repository.save(routine);
    return routine;
  }

  @Override
  public Routine update(String ownerId, UUID id, RoutineContent content) {
    Routine routine = get(ownerId, id);
    requirePublished(content);
    routine.edit(content);
    repository.save(routine);
    return routine;
  }

  @Override
  public void delete(String ownerId, UUID id) {
    get(ownerId, id);
    repository.deleteById(id);
  }

  private void requirePublished(RoutineContent content) {
    for (MovementRef ref : content.allReferences()) {
      if (!catalog.isPublished(ref.key(), ref.revision())) {
        throw new IllegalArgumentException(
            "Movement " + ref.key() + " revision " + ref.revision() + " is not published");
      }
    }
  }
}
