package com.tglanz.atlas.backend.routine.application.service;

import com.tglanz.atlas.backend.routine.application.port.in.RoutineUseCase;
import com.tglanz.atlas.backend.routine.application.port.out.MovementCatalog;
import com.tglanz.atlas.backend.routine.application.port.out.RoutineRepository;
import com.tglanz.atlas.backend.routine.domain.Routine;
import com.tglanz.atlas.backend.routine.domain.RoutineContent;
import com.tglanz.atlas.backend.routine.domain.Section;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoutineService implements RoutineUseCase {

  private final RoutineRepository repository;
  private final MovementCatalog catalog;

  public RoutineService(RoutineRepository repository, MovementCatalog catalog) {
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
    requireExisting(content);
    Routine routine = Routine.create(ownerId, content);
    repository.save(routine);
    return routine;
  }

  @Override
  public Routine update(String ownerId, UUID id, RoutineContent content) {
    Routine routine = get(ownerId, id);
    requireExisting(content);
    routine.edit(content);
    repository.save(routine);
    return routine;
  }

  @Override
  public Routine addMovement(String ownerId, UUID id, int sectionIndex, String movementKey) {
    Routine routine = get(ownerId, id);
    var sections = new ArrayList<>(routine.content().sections());
    if (sectionIndex < 0 || sectionIndex >= Math.max(1, sections.size())) {
      throw new IllegalArgumentException("Choose an existing section");
    }
    if (sections.isEmpty()) {
      sections.add(new Section("Practice", "", List.of(movementKey)));
    } else {
      Section section = sections.get(sectionIndex);
      var keys = new ArrayList<>(section.movements());
      keys.add(movementKey);
      sections.set(sectionIndex, new Section(section.name(), section.description(), keys));
    }
    RoutineContent content =
        new RoutineContent(routine.content().name(), routine.content().description(), sections);
    requireExisting(content);
    routine.edit(content);
    repository.save(routine);
    return routine;
  }

  @Override
  @Transactional(readOnly = true)
  public Routine getShared(UUID token) {
    return repository
        .findByShareToken(token)
        .orElseThrow(() -> new NotFoundException("This shared routine is unavailable"));
  }

  @Override
  public UUID enableSharing(String ownerId, UUID id) {
    get(ownerId, id);
    return repository
        .enableSharing(id)
        .orElseThrow(() -> new NotFoundException("Routine not found"));
  }

  @Override
  public void disableSharing(String ownerId, UUID id) {
    get(ownerId, id);
    repository.disableSharing(id);
  }

  @Override
  public void delete(String ownerId, UUID id) {
    get(ownerId, id);
    repository.deleteById(id);
  }

  private void requireExisting(RoutineContent content) {
    for (String key : content.movementKeys()) {
      if (!catalog.exists(key)) {
        throw new IllegalArgumentException("Movement " + key + " does not exist");
      }
    }
  }
}
