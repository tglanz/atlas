package com.tglanz.atlas.backend.movement.adapter.out.persistence;

import com.tglanz.atlas.backend.movement.application.port.out.MovementRepository;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementItem;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementName;
import com.tglanz.atlas.backend.movement.domain.MovementStage;
import com.tglanz.atlas.backend.movement.domain.TagCount;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
class MovementPersistenceAdapter implements MovementRepository {

  private final MovementJpaRepository jpa;

  MovementPersistenceAdapter(MovementJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public boolean exists(MovementKey key) {
    return jpa.existsById(key.value());
  }

  @Override
  public Optional<Movement> find(MovementKey key) {
    return jpa.findById(key.value()).map(this::toDomain);
  }

  @Override
  public List<Movement> search(String text, Set<String> tags) {
    // An empty list cannot be bound in a query, so a placeholder stands in when no tags are given.
    Collection<String> bound = tags.isEmpty() ? List.of("") : tags;
    return jpa.search(text, bound, tags.size()).stream().map(this::toDomain).toList();
  }

  @Override
  public List<MovementName> findAllNames() {
    return jpa.findNames().stream()
        .map(row -> new MovementName(new MovementKey((String) row[0]), (String) row[1]))
        .toList();
  }

  @Override
  public List<TagCount> countTags() {
    return jpa.countTags().stream()
        .map(row -> new TagCount((String) row[0], ((Number) row[1]).longValue()))
        .toList();
  }

  @Override
  public void save(Movement movement) {
    MovementEntity entity =
        jpa.findById(movement.key().value())
            .orElseGet(
                () -> {
                  var created = new MovementEntity();
                  created.movementKey = movement.key().value();
                  return created;
                });
    MovementContent content = movement.content();
    entity.name = content.name();
    entity.description = content.description();
    entity.items.clear();
    for (MovementItem item : content.items()) {
      var row = new MovementEntity.ItemRow();
      row.type = item.type();
      row.content = item.content();
      entity.items.add(row);
    }
    entity.stages.clear();
    for (MovementStage stage : content.stages()) {
      var row = new MovementEntity.StageRow();
      row.name = stage.name();
      row.description = stage.description();
      entity.stages.add(row);
    }
    entity.tags.clear();
    entity.tags.addAll(content.tags());
    entity.illustrations.clear();
    for (Illustration illustration : movement.illustrations()) {
      var row = new MovementEntity.IllustrationRow();
      row.imageUrl = illustration.imageUrl();
      entity.illustrations.add(row);
    }
    jpa.saveAndFlush(entity);
  }

  private Movement toDomain(MovementEntity e) {
    List<MovementItem> items =
        e.items.stream().map(row -> new MovementItem(row.type, row.content)).toList();
    List<MovementStage> stages =
        e.stages.stream().map(row -> new MovementStage(row.name, row.description)).toList();
    MovementContent content =
        new MovementContent(e.name, e.description, items, stages, Set.copyOf(e.tags));
    List<Illustration> illustrations =
        e.illustrations.stream().map(row -> new Illustration(row.imageUrl)).toList();
    return Movement.restore(new MovementKey(e.movementKey), content, illustrations);
  }
}
