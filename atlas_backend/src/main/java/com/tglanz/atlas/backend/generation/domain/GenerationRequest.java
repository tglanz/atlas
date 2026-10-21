package com.tglanz.atlas.backend.generation.domain;

import com.tglanz.atlas.backend.shared.error.ConflictException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A request to generate the Illustration sequence of one Avatar for an open Movement draft. One
 * Illustration is produced per stage, in stage order.
 */
public final class GenerationRequest {
  private static final int MAX_STAGES = 20;

  private final UUID id;
  private final String movementKey;
  private final int draftRevision;
  private final UUID avatarId;
  private final UUID viewpointId;
  private final List<Stage> stages;
  private final Instant createdAt;
  private GenerationStatus status;
  private String failureReason;
  private List<String> resultUrls;
  private Instant completedAt;

  private GenerationRequest(
      UUID id,
      String movementKey,
      int draftRevision,
      UUID avatarId,
      UUID viewpointId,
      List<Stage> stages,
      GenerationStatus status,
      String failureReason,
      List<String> resultUrls,
      Instant createdAt,
      Instant completedAt) {
    if (id == null || movementKey == null || avatarId == null || viewpointId == null) {
      throw new IllegalArgumentException("Movement, avatar and viewpoint are required");
    }
    if (stages == null || stages.isEmpty() || stages.size() > MAX_STAGES) {
      throw new IllegalArgumentException(
          "Between 1 and %d stages are required".formatted(MAX_STAGES));
    }
    this.id = id;
    this.movementKey = movementKey;
    this.draftRevision = draftRevision;
    this.avatarId = avatarId;
    this.viewpointId = viewpointId;
    this.stages = List.copyOf(stages);
    this.status = status;
    this.failureReason = failureReason;
    this.resultUrls = List.copyOf(resultUrls);
    this.createdAt = createdAt;
    this.completedAt = completedAt;
  }

  public static GenerationRequest create(
      String movementKey,
      int draftRevision,
      UUID avatarId,
      UUID viewpointId,
      List<Stage> stages,
      Instant now) {
    return new GenerationRequest(
        UUID.randomUUID(),
        movementKey,
        draftRevision,
        avatarId,
        viewpointId,
        stages,
        GenerationStatus.PENDING,
        null,
        List.of(),
        now,
        null);
  }

  public static GenerationRequest restore(
      UUID id,
      String movementKey,
      int draftRevision,
      UUID avatarId,
      UUID viewpointId,
      List<Stage> stages,
      GenerationStatus status,
      String failureReason,
      List<String> resultUrls,
      Instant createdAt,
      Instant completedAt) {
    return new GenerationRequest(
        id,
        movementKey,
        draftRevision,
        avatarId,
        viewpointId,
        stages,
        status,
        failureReason,
        resultUrls,
        createdAt,
        completedAt);
  }

  public void start() {
    require(GenerationStatus.PENDING, "Only pending requests can start");
    status = GenerationStatus.RUNNING;
  }

  public void succeed(List<String> urls, Instant now) {
    require(GenerationStatus.RUNNING, "Only running requests can succeed");
    if (urls.size() != stages.size()) {
      throw new IllegalArgumentException("Expected one illustration per stage");
    }
    resultUrls = List.copyOf(urls);
    status = GenerationStatus.SUCCEEDED;
    completedAt = now;
  }

  public void fail(String reason, Instant now) {
    if (status == GenerationStatus.SUCCEEDED || status == GenerationStatus.FAILED) {
      throw new ConflictException("Request already finished");
    }
    failureReason = reason;
    status = GenerationStatus.FAILED;
    completedAt = now;
  }

  /** Runs a failed request again. */
  public void retry() {
    require(GenerationStatus.FAILED, "Only failed requests can be retried");
    status = GenerationStatus.PENDING;
    failureReason = null;
    completedAt = null;
  }

  private void require(GenerationStatus expected, String message) {
    if (status != expected) {
      throw new ConflictException(message);
    }
  }

  public UUID id() {
    return id;
  }

  public String movementKey() {
    return movementKey;
  }

  public int draftRevision() {
    return draftRevision;
  }

  public UUID avatarId() {
    return avatarId;
  }

  public UUID viewpointId() {
    return viewpointId;
  }

  public List<Stage> stages() {
    return stages;
  }

  public GenerationStatus status() {
    return status;
  }

  public String failureReason() {
    return failureReason;
  }

  public List<String> resultUrls() {
    return resultUrls;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant completedAt() {
    return completedAt;
  }
}
