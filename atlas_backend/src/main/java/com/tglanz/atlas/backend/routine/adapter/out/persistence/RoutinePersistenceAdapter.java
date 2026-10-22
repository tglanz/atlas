package com.tglanz.atlas.backend.routine.adapter.out.persistence;

import com.tglanz.atlas.backend.routine.application.port.out.RoutineRepository;
import com.tglanz.atlas.backend.routine.domain.Routine;
import com.tglanz.atlas.backend.routine.domain.RoutineContent;
import com.tglanz.atlas.backend.routine.domain.Section;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class RoutinePersistenceAdapter implements RoutineRepository {

  private final RoutineJpaRepository jpa;

  RoutinePersistenceAdapter(RoutineJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public List<Routine> findByOwner(String ownerId) {
    return jpa.findByOwnerIdOrderByNameAsc(ownerId).stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<Routine> findById(UUID id) {
    return jpa.findById(id.toString()).map(this::toDomain);
  }

  @Override
  public void save(Routine routine) {
    RoutineEntity entity =
        jpa.findById(routine.id().toString())
            .orElseGet(
                () -> {
                  var created = new RoutineEntity();
                  created.id = routine.id().toString();
                  created.ownerId = routine.ownerId();
                  return created;
                });
    RoutineContent content = routine.content();
    entity.name = content.name();
    entity.description = content.description();
    entity.sections.clear();
    int position = 0;
    for (Section section : content.sections()) {
      var row = new RoutineEntity.SectionEntity();
      row.id = UUID.randomUUID().toString();
      row.routine = entity;
      row.position = position++;
      row.name = section.name();
      row.description = section.description();
      row.movements.addAll(section.movements());
      entity.sections.add(row);
    }
    jpa.saveAndFlush(entity);
  }

  @Override
  public void deleteById(UUID id) {
    jpa.deleteById(id.toString());
    jpa.flush();
  }

  private Routine toDomain(RoutineEntity e) {
    List<Section> sections =
        e.sections.stream()
            .map(s -> new Section(s.name, s.description, List.copyOf(s.movements)))
            .toList();
    return Routine.restore(
        UUID.fromString(e.id), e.ownerId, new RoutineContent(e.name, e.description, sections));
  }
}
