package com.tglanz.atlas.backend.movement.adapter.out.persistence;

import com.tglanz.atlas.backend.movement.application.port.out.MovementRepository;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementLink;
import com.tglanz.atlas.backend.movement.domain.RevisionStatus;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class MovementPersistenceAdapter implements MovementRepository {

  private final MovementRevisionJpaRepository jpa;

  MovementPersistenceAdapter(MovementRevisionJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public boolean existsKey(MovementKey key) {
    return jpa.existsByMovementKey(key.value());
  }

  @Override
  public Optional<Movement> find(MovementKey key, int revision) {
    return jpa.findByMovementKeyAndRevision(key.value(), revision).map(this::toDomain);
  }

  @Override
  public Optional<Movement> findLatestPublished(MovementKey key) {
    return jpa.findFirstByMovementKeyAndStatusOrderByRevisionDesc(
            key.value(), RevisionStatus.PUBLISHED)
        .map(this::toDomain);
  }

  @Override
  public Optional<Movement> findDraft(MovementKey key) {
    return jpa.findFirstByMovementKeyAndStatusOrderByRevisionDesc(key.value(), RevisionStatus.DRAFT)
        .map(this::toDomain);
  }

  @Override
  public List<Movement> findAllRevisions(MovementKey key) {
    return jpa.findByMovementKeyOrderByRevisionAsc(key.value()).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public List<Movement> searchCurrentPublished(String text, String tag) {
    return jpa.searchCurrentPublished(text, tag).stream().map(this::toDomain).toList();
  }

  @Override
  public void save(Movement movement) {
    MovementRevisionEntity entity =
        jpa.findByMovementKeyAndRevision(movement.key().value(), movement.revision())
            .orElseGet(
                () -> {
                  var created = new MovementRevisionEntity();
                  created.id = UUID.randomUUID().toString();
                  created.movementKey = movement.key().value();
                  created.revision = movement.revision();
                  return created;
                });
    entity.status = movement.status();
    entity.publishedAt = movement.publishedAt();
    MovementContent content = movement.content();
    entity.name = content.name();
    entity.description = content.description();
    entity.cues.clear();
    entity.cues.addAll(content.cues());
    entity.tags.clear();
    entity.tags.addAll(content.tags());
    entity.links.clear();
    for (MovementLink link : content.links()) {
      var row = new MovementRevisionEntity.LinkRow();
      row.targetKey = link.target().value();
      row.relationType = link.relationType();
      entity.links.add(row);
    }
    entity.illustrations.clear();
    movement
        .illustrations()
        .forEach(
            (avatarId, list) -> {
              for (Illustration illustration : list) {
                var row = new MovementRevisionEntity.IllustrationRow();
                row.avatarId = avatarId.toString();
                row.url = illustration.url();
                entity.illustrations.add(row);
              }
            });
    jpa.saveAndFlush(entity);
  }

  private Movement toDomain(MovementRevisionEntity e) {
    List<MovementLink> links = new ArrayList<>();
    for (var row : e.links) {
      links.add(new MovementLink(new MovementKey(row.targetKey), row.relationType));
    }
    MovementContent content =
        new MovementContent(e.name, e.description, List.copyOf(e.cues), Set.copyOf(e.tags), links);
    Map<UUID, List<Illustration>> illustrations = new LinkedHashMap<>();
    for (var row : e.illustrations) {
      illustrations
          .computeIfAbsent(UUID.fromString(row.avatarId), id -> new ArrayList<>())
          .add(new Illustration(row.url));
    }
    return Movement.restore(
        new MovementKey(e.movementKey),
        e.revision,
        e.status,
        e.publishedAt,
        content,
        illustrations);
  }
}
