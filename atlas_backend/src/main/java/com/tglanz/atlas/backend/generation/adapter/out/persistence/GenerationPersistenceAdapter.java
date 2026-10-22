package com.tglanz.atlas.backend.generation.adapter.out.persistence;

import com.tglanz.atlas.backend.generation.application.port.out.GenerationRepository;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations;
import com.tglanz.atlas.backend.generation.domain.GenerationStatus;
import com.tglanz.atlas.backend.generation.domain.IllustrationCandidate;
import com.tglanz.atlas.backend.generation.domain.IllustrationStage;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.persistence.ConstraintViolations;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
class GenerationPersistenceAdapter implements GenerationRepository {

  private static final Set<GenerationStatus> IN_PROGRESS =
      Set.of(GenerationStatus.PENDING, GenerationStatus.RUNNING);

  private final GenerationJpaRepository jpa;

  GenerationPersistenceAdapter(GenerationJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<GenerationMovementIllustrations> findById(UUID id) {
    return jpa.findById(id.toString()).map(this::toDomain);
  }

  @Override
  public List<GenerationMovementIllustrations> findByMovementKey(String movementKey) {
    return jpa.findByMovementKeyOrderByCreatedAtDesc(movementKey).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public boolean existsInProgress(String movementKey) {
    return jpa.existsByMovementKeyAndStatusIn(movementKey, IN_PROGRESS);
  }

  @Override
  public List<GenerationMovementIllustrations> findInProgress() {
    return jpa.findByStatusIn(IN_PROGRESS).stream().map(this::toDomain).toList();
  }

  @Override
  public void save(GenerationMovementIllustrations generation) {
    GenerationEntity entity =
        jpa.findById(generation.id().toString())
            .orElseGet(
                () -> {
                  var created = new GenerationEntity();
                  created.id = generation.id().toString();
                  created.movementKey = generation.movementKey();
                  created.createdAt = generation.createdAt();
                  return created;
                });
    entity.status = generation.status();
    entity.failureReason = generation.failureReason();
    entity.finishedAt = generation.finishedAt();
    entity.stages.clear();
    generation.stages().forEach(s -> entity.stages.add(toRow(s)));
    try {
      jpa.saveAndFlush(entity);
    } catch (DataAccessException e) {
      if (!ConstraintViolations.isConstraintViolation(e)) {
        throw e;
      }
      throw new ConflictException("This movement already has a generation in progress");
    }
  }

  private static GenerationEntity.StageRow toRow(IllustrationStage stage) {
    var row = new GenerationEntity.StageRow();
    row.avatarId = stage.avatarId().toString();
    row.viewpointId = stage.viewpointId().toString();
    row.name = stage.name();
    row.description = stage.description();
    IllustrationCandidate c = stage.candidate();
    row.candidateState = c.state();
    row.candidateImageUrl = c.imageUrl();
    row.candidateFailureReason = c.failureReason();
    row.candidateProgress = c.progress();
    row.provider = c.provider();
    row.model = c.model();
    row.providerRequestId = c.providerRequestId();
    row.providerStatusUrl = c.providerStatusUrl();
    row.providerResultUrl = c.providerResultUrl();
    row.obtainedAt = c.obtainedAt();
    return row;
  }

  private GenerationMovementIllustrations toDomain(GenerationEntity e) {
    List<IllustrationStage> stages =
        java.util.stream.IntStream.range(0, e.stages.size())
            .mapToObj(
                i -> {
                  var r = e.stages.get(i);
                  var candidate =
                      new IllustrationCandidate(
                          r.candidateState,
                          r.candidateImageUrl,
                          r.candidateFailureReason,
                          r.candidateProgress,
                          r.provider,
                          r.model,
                          r.providerRequestId,
                          r.providerStatusUrl,
                          r.providerResultUrl,
                          r.obtainedAt);
                  return new IllustrationStage(
                      i,
                      UUID.fromString(r.avatarId),
                      UUID.fromString(r.viewpointId),
                      r.name,
                      r.description,
                      candidate);
                })
            .toList();
    return GenerationMovementIllustrations.restore(
        UUID.fromString(e.id),
        e.movementKey,
        e.status,
        e.failureReason,
        e.createdAt,
        e.finishedAt,
        stages);
  }
}
