package com.tglanz.atlas.backend.generation.adapter.out.persistence;

import com.tglanz.atlas.backend.generation.application.port.out.GenerationRepository;
import com.tglanz.atlas.backend.generation.domain.AvatarImageSpec;
import com.tglanz.atlas.backend.generation.domain.GenerationRequest;
import com.tglanz.atlas.backend.generation.domain.IllustrationsGenerationSpec;
import com.tglanz.atlas.backend.generation.domain.StageSpec;
import com.tglanz.atlas.backend.generation.domain.StaticImageSpec;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class GenerationPersistenceAdapter implements GenerationRepository {

  private static final String AVATAR = "AVATAR";
  private static final String STATIC = "STATIC";

  private final GenerationRequestJpaRepository jpa;

  GenerationPersistenceAdapter(GenerationRequestJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<GenerationRequest> findById(UUID id) {
    return jpa.findById(id.toString()).map(this::toDomain);
  }

  @Override
  public List<GenerationRequest> findByMovementKey(String movementKey) {
    return jpa.findByMovementKeyOrderByCreatedAtDesc(movementKey).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public void save(GenerationRequest request) {
    GenerationRequestEntity entity =
        jpa.findById(request.id().toString())
            .orElseGet(
                () -> {
                  var created = new GenerationRequestEntity();
                  created.id = request.id().toString();
                  created.movementKey = request.movementKey();
                  created.createdAt = request.createdAt();
                  request.spec().stages().forEach(stage -> created.stages.add(toRow(stage)));
                  return created;
                });
    entity.status = request.status();
    entity.failureReason = request.failureReason();
    entity.completedAt = request.completedAt();
    entity.results.clear();
    entity.results.addAll(request.resultUrls());
    jpa.saveAndFlush(entity);
  }

  private static GenerationRequestEntity.StageRow toRow(StageSpec stage) {
    var row = new GenerationRequestEntity.StageRow();
    row.instructions = "";
    row.referenceImageUrl = "";
    row.imageUrl = "";
    switch (stage) {
      case AvatarImageSpec s -> {
        row.kind = AVATAR;
        row.avatarId = s.avatarId().toString();
        row.viewpointId = s.viewpointId().toString();
        row.instructions = s.instructions();
        row.referenceImageUrl = s.referenceImageUrl();
      }
      case StaticImageSpec s -> {
        row.kind = STATIC;
        row.imageUrl = s.imageUrl();
      }
    }
    return row;
  }

  private static StageSpec toStage(GenerationRequestEntity.StageRow row) {
    return switch (row.kind) {
      case AVATAR ->
          new AvatarImageSpec(
              UUID.fromString(row.avatarId),
              UUID.fromString(row.viewpointId),
              row.instructions,
              row.referenceImageUrl);
      case STATIC -> new StaticImageSpec(row.imageUrl);
      default -> throw new IllegalStateException("Unknown stage kind " + row.kind);
    };
  }

  private GenerationRequest toDomain(GenerationRequestEntity e) {
    var spec =
        new IllustrationsGenerationSpec(
            e.stages.stream().map(GenerationPersistenceAdapter::toStage).toList());
    return GenerationRequest.restore(
        UUID.fromString(e.id),
        e.movementKey,
        spec,
        e.status,
        e.failureReason,
        List.copyOf(e.results),
        e.createdAt,
        e.completedAt);
  }
}
