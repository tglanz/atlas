package com.tglanz.atlas.backend.generation.adapter.out.persistence;

import com.tglanz.atlas.backend.generation.application.port.out.GenerationRepository;
import com.tglanz.atlas.backend.generation.domain.GenerationRequest;
import com.tglanz.atlas.backend.generation.domain.Stage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class GenerationPersistenceAdapter implements GenerationRepository {

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
                  created.draftRevision = request.draftRevision();
                  created.avatarId = request.avatarId().toString();
                  created.viewpointId = request.viewpointId().toString();
                  created.createdAt = request.createdAt();
                  for (Stage stage : request.stages()) {
                    var row = new GenerationRequestEntity.StageRow();
                    row.description = stage.description();
                    row.referenceUrl = stage.referenceUrl();
                    created.stages.add(row);
                  }
                  return created;
                });
    entity.status = request.status();
    entity.failureReason = request.failureReason();
    entity.completedAt = request.completedAt();
    entity.results.clear();
    entity.results.addAll(request.resultUrls());
    jpa.saveAndFlush(entity);
  }

  private GenerationRequest toDomain(GenerationRequestEntity e) {
    List<Stage> stages =
        e.stages.stream().map(s -> new Stage(s.description, s.referenceUrl)).toList();
    return GenerationRequest.restore(
        UUID.fromString(e.id),
        e.movementKey,
        e.draftRevision,
        UUID.fromString(e.avatarId),
        UUID.fromString(e.viewpointId),
        stages,
        e.status,
        e.failureReason,
        List.copyOf(e.results),
        e.createdAt,
        e.completedAt);
  }
}
