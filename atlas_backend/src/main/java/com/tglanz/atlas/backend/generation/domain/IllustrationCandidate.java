package com.tglanz.atlas.backend.generation.domain;

import java.time.Instant;

/**
 * The image generated for an IllustrationStage, with the provider details of the call that made it.
 * It is not on the Movement until approved.
 */
public record IllustrationCandidate(
    CandidateState state,
    String imageUrl,
    String failureReason,
    String progress,
    String provider,
    String model,
    String providerRequestId,
    String providerStatusUrl,
    String providerResultUrl,
    Instant obtainedAt) {

  public static IllustrationCandidate pending() {
    return new IllustrationCandidate(
        CandidateState.PENDING, null, null, null, null, null, null, null, null, null);
  }

  /** Remembers the provider request as soon as it is submitted, before it finishes. */
  public IllustrationCandidate submitted(
      String provider, String model, String requestId, String statusUrl, String resultUrl) {
    return new IllustrationCandidate(
        state,
        imageUrl,
        failureReason,
        progress,
        provider,
        model,
        requestId,
        statusUrl,
        resultUrl,
        obtainedAt);
  }

  /** What the provider reports while the image is being made, such as its place in the queue. */
  public IllustrationCandidate withProgress(String text) {
    return new IllustrationCandidate(
        state,
        imageUrl,
        failureReason,
        text,
        provider,
        model,
        providerRequestId,
        providerStatusUrl,
        providerResultUrl,
        obtainedAt);
  }

  public IllustrationCandidate done(String url, Instant at) {
    return new IllustrationCandidate(
        CandidateState.DONE,
        url,
        null,
        null,
        provider,
        model,
        providerRequestId,
        providerStatusUrl,
        providerResultUrl,
        at);
  }

  public IllustrationCandidate failed(String reason) {
    return new IllustrationCandidate(
        CandidateState.FAILED,
        null,
        reason,
        null,
        provider,
        model,
        providerRequestId,
        providerStatusUrl,
        providerResultUrl,
        null);
  }
}
