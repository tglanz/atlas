package com.tglanz.atlas.backend.generation.domain;

import com.tglanz.atlas.backend.shared.error.ConflictException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The generation state for a Movement's Illustrations: a maintainer's order to generate one image
 * per stage. Its candidates are only proposals. They reach the Movement when the generation is
 * approved.
 */
public final class GenerationMovementIllustrations {
  private static final int MAX_STAGES = 20;

  /** What a maintainer asks for in one stage. */
  public record StageDefinition(UUID avatarId, UUID viewpointId, String name, String description) {}

  private final UUID id;
  private final String movementKey;
  private final Instant createdAt;
  private GenerationStatus status;
  private String failureReason;
  private Instant finishedAt;
  private List<IllustrationStage> stages;

  private GenerationMovementIllustrations(
      UUID id,
      String movementKey,
      GenerationStatus status,
      String failureReason,
      Instant createdAt,
      Instant finishedAt,
      List<IllustrationStage> stages) {
    if (id == null || movementKey == null || status == null) {
      throw new IllegalArgumentException("Id, movement and status are required");
    }
    if (stages == null || stages.isEmpty() || stages.size() > MAX_STAGES) {
      throw new IllegalArgumentException(
          "Between 1 and %d stages are required".formatted(MAX_STAGES));
    }
    this.id = id;
    this.movementKey = movementKey;
    this.status = status;
    this.failureReason = failureReason;
    this.createdAt = createdAt;
    this.finishedAt = finishedAt;
    this.stages = List.copyOf(stages);
  }

  public static GenerationMovementIllustrations create(
      String movementKey, List<StageDefinition> definitions, Instant now) {
    if (definitions == null) {
      throw new IllegalArgumentException("Stages are required");
    }
    List<IllustrationStage> stages = new ArrayList<>();
    for (int i = 0; i < definitions.size(); i++) {
      StageDefinition d = definitions.get(i);
      stages.add(
          new IllustrationStage(
              i,
              d.avatarId(),
              d.viewpointId(),
              d.name(),
              d.description(),
              IllustrationCandidate.pending()));
    }
    return new GenerationMovementIllustrations(
        UUID.randomUUID(), movementKey, GenerationStatus.PENDING, null, now, null, stages);
  }

  public static GenerationMovementIllustrations restore(
      UUID id,
      String movementKey,
      GenerationStatus status,
      String failureReason,
      Instant createdAt,
      Instant finishedAt,
      List<IllustrationStage> stages) {
    return new GenerationMovementIllustrations(
        id, movementKey, status, failureReason, createdAt, finishedAt, stages);
  }

  public boolean isInProgress() {
    return status == GenerationStatus.PENDING || status == GenerationStatus.RUNNING;
  }

  public void start() {
    require(GenerationStatus.PENDING, "Only a pending generation can start");
    status = GenerationStatus.RUNNING;
  }

  public void recordSubmission(
      int position,
      String provider,
      String model,
      String requestId,
      String statusUrl,
      String resultUrl) {
    requireRunning();
    replace(
        position,
        stage(position).candidate().submitted(provider, model, requestId, statusUrl, resultUrl));
  }

  public void recordProgress(int position, String progress) {
    requireRunning();
    replace(position, stage(position).candidate().withProgress(progress));
  }

  /** Stores the image. When every stage is done, the generation is ready for review. */
  public void recordDone(int position, String imageUrl, Instant now) {
    requireRunning();
    replace(position, stage(position).candidate().done(imageUrl, now));
    if (stages.stream().allMatch(s -> s.candidate().state() == CandidateState.DONE)) {
      status = GenerationStatus.READY_FOR_REVIEW;
      finishedAt = now;
    }
  }

  /** The first stage that fails fails the whole generation. */
  public void recordFailed(int position, String reason, Instant now) {
    requireRunning();
    replace(position, stage(position).candidate().failed(reason));
    fail("Stage %d failed: %s".formatted(position + 1, reason), now);
  }

  /** For a generation cut short by something outside it, such as a restart. */
  public void failInterrupted(String reason, Instant now) {
    if (!isInProgress()) {
      throw new ConflictException("Only a generation in progress can be interrupted");
    }
    fail(reason, now);
  }

  /** Approves: returns the image urls to put on the Movement, in stage order. */
  public List<String> approve() {
    require(
        GenerationStatus.READY_FOR_REVIEW, "Only a generation ready for review can be approved");
    status = GenerationStatus.APPLIED;
    return stages.stream().map(s -> s.candidate().imageUrl()).toList();
  }

  public void discard() {
    require(
        GenerationStatus.READY_FOR_REVIEW, "Only a generation ready for review can be discarded");
    status = GenerationStatus.DISCARDED;
  }

  private void fail(String reason, Instant now) {
    status = GenerationStatus.FAILED;
    failureReason = reason;
    finishedAt = now;
  }

  private IllustrationStage stage(int position) {
    return stages.stream()
        .filter(s -> s.position() == position)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No stage " + position));
  }

  private void replace(int position, IllustrationCandidate candidate) {
    stages =
        stages.stream()
            .map(s -> s.position() == position ? s.withCandidate(candidate) : s)
            .toList();
  }

  private void require(GenerationStatus expected, String message) {
    if (status != expected) {
      throw new ConflictException(message);
    }
  }

  private void requireRunning() {
    require(GenerationStatus.RUNNING, "The generation is not running");
  }

  public UUID id() {
    return id;
  }

  public String movementKey() {
    return movementKey;
  }

  public GenerationStatus status() {
    return status;
  }

  public String failureReason() {
    return failureReason;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant finishedAt() {
    return finishedAt;
  }

  public List<IllustrationStage> stages() {
    return stages;
  }
}
