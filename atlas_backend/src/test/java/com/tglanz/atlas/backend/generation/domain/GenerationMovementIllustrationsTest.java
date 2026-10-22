package com.tglanz.atlas.backend.generation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations.StageDefinition;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GenerationMovementIllustrationsTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  private static GenerationMovementIllustrations running() {
    var stage = new StageDefinition(UUID.randomUUID(), UUID.randomUUID(), "start");
    var g = GenerationMovementIllustrations.create("push-up", List.of(stage, stage), NOW);
    g.start();
    return g;
  }

  @Test
  void isReadyForReviewWhenEveryStageIsDone() {
    var g = running();
    g.recordDone(0, "https://x/0.png", NOW);
    assertThat(g.status()).isEqualTo(GenerationStatus.RUNNING);
    g.recordDone(1, "https://x/1.png", NOW);
    assertThat(g.status()).isEqualTo(GenerationStatus.READY_FOR_REVIEW);
    assertThat(g.approve()).containsExactly("https://x/0.png", "https://x/1.png");
    assertThat(g.status()).isEqualTo(GenerationStatus.APPLIED);
  }

  @Test
  void firstFailedStageFailsTheGeneration() {
    var g = running();
    g.recordDone(0, "https://x/0.png", NOW);
    g.recordFailed(1, "refused", NOW);
    assertThat(g.status()).isEqualTo(GenerationStatus.FAILED);
    assertThat(g.stages().get(0).candidate().state()).isEqualTo(CandidateState.DONE);
    assertThatThrownBy(g::approve).isInstanceOf(ConflictException.class);
  }

  @Test
  void canOnlyBeDiscardedWhenReadyForReview() {
    var g = running();
    assertThatThrownBy(g::discard).isInstanceOf(ConflictException.class);
  }

  @Test
  void needsAtLeastOneStage() {
    assertThatThrownBy(() -> GenerationMovementIllustrations.create("push-up", List.of(), NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
