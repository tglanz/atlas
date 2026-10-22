package com.tglanz.atlas.backend.generation.domain;

import com.tglanz.atlas.backend.shared.error.ConflictException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A request to produce the Illustration sequence of an open Movement draft from a spec. A
 * successful request replaces the draft's sequence: one Illustration per stage, in order.
 */
public final class GenerationRequest {
  private final UUID id;
  private final String movementKey;
  private final int draftRevision;
  private final IllustrationsGenerationSpec spec;
  private final Instant createdAt;
  private GenerationStatus status;
  private String failureReason;
  private List<String> resultUrls;
  private Instant completedAt;

  private GenerationRequest(
      UUID id,
      String movementKey,
      int draftRevision,
      IllustrationsGenerationSpec spec,
      GenerationStatus status,
      String failureReason,
      List<String> resultUrls,
      Instant createdAt,
      Instant completedAt) {
    if (id == null || movementKey == null || spec == null) {
      throw new IllegalArgumentException("Movement and spec are required");
    }
    this.id = id;
    this.movementKey = movementKey;
    this.draftRevision = draftRevision;
    this.spec = spec;
    this.status = status;
    this.failureReason = failureReason;
    this.resultUrls = List.copyOf(resultUrls);
    this.createdAt = createdAt;
    this.completedAt = completedAt;
  }

  public static GenerationRequest create(
      String movementKey, int draftRevision, IllustrationsGenerationSpec spec, Instant now) {
    return new GenerationRequest(
        UUID.randomUUID(),
        movementKey,
        draftRevision,
        spec,
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
      IllustrationsGenerationSpec spec,
      GenerationStatus status,
      String failureReason,
      List<String> resultUrls,
      Instant createdAt,
      Instant completedAt) {
    return new GenerationRequest(
        id,
        movementKey,
        draftRevision,
        spec,
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
    if (urls.size() != spec.stages().size()) {
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

  public IllustrationsGenerationSpec spec() {
    return spec;
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
