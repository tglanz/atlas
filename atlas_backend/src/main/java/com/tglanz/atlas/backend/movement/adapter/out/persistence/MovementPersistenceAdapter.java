package com.tglanz.atlas.backend.movement.adapter.out.persistence;

import com.tglanz.atlas.backend.movement.application.port.out.MovementRepository;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementLink;
import java.util.ArrayList;
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
  public List<Movement> search(String text, String tag) {
    return jpa.search(text, tag).stream().map(this::toDomain).toList();
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
    entity.cues.clear();
    entity.cues.addAll(content.cues());
    entity.tags.clear();
    entity.tags.addAll(content.tags());
    entity.links.clear();
    for (MovementLink link : content.links()) {
      var row = new MovementEntity.LinkRow();
      row.targetKey = link.target().value();
      row.relationType = link.relationType();
      entity.links.add(row);
    }
    entity.illustrations.clear();
    for (Illustration illustration : movement.illustrations()) {
      var row = new MovementEntity.IllustrationRow();
      row.url = illustration.url();
      entity.illustrations.add(row);
    }
    jpa.saveAndFlush(entity);
  }

  private Movement toDomain(MovementEntity e) {
    List<MovementLink> links = new ArrayList<>();
    for (var row : e.links) {
      links.add(new MovementLink(new MovementKey(row.targetKey), row.relationType));
    }
    MovementContent content =
        new MovementContent(e.name, e.description, List.copyOf(e.cues), Set.copyOf(e.tags), links);
    List<Illustration> illustrations =
        e.illustrations.stream().map(row -> new Illustration(row.url)).toList();
    return Movement.restore(new MovementKey(e.movementKey), content, illustrations);
  }
}
