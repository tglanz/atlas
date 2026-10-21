package com.tglanz.atlas.backend.generation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tglanz.atlas.backend.shared.error.ConflictException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GenerationRequestTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  private static GenerationRequest request() {
    return GenerationRequest.create(
        "push-up",
        1,
        UUID.randomUUID(),
        UUID.randomUUID(),
        List.of(
            new Stage("Chest near the floor", ""), new Stage("", "https://example.com/top.png")),
        NOW);
  }

  @Test
  void runsFromPendingToSucceededWithOneResultPerStage() {
    GenerationRequest r = request();
    assertThat(r.status()).isEqualTo(GenerationStatus.PENDING);
    r.start();
    r.succeed(List.of("a", "b"), NOW);
    assertThat(r.status()).isEqualTo(GenerationStatus.SUCCEEDED);
    assertThat(r.resultUrls()).containsExactly("a", "b");
  }

  @Test
  void resultCountMustMatchStages() {
    GenerationRequest r = request();
    r.start();
    assertThatThrownBy(() -> r.succeed(List.of("a"), NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void failedRequestsCanBeRetriedOthersCannot() {
    GenerationRequest r = request();
    assertThatThrownBy(r::retry).isInstanceOf(ConflictException.class);
    r.start();
    r.fail("boom", NOW);
    assertThat(r.failureReason()).isEqualTo("boom");
    r.retry();
    assertThat(r.status()).isEqualTo(GenerationStatus.PENDING);
    assertThat(r.failureReason()).isNull();
  }

  @Test
  void stageNeedsDescriptionOrReference() {
    assertThatThrownBy(() -> new Stage(" ", "")).isInstanceOf(IllegalArgumentException.class);
    assertThat(new Stage("", "https://example.com/x.png").referenceUrl()).isNotBlank();
  }
}
