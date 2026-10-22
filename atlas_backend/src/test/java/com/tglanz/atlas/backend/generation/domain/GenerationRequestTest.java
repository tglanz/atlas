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

  /** One generated stage and one static stage: the model allows mixing. */
  private static GenerationRequest request() {
    var spec =
        new IllustrationsGenerationSpec(
            List.of(
                new AvatarImageSpec(
                    UUID.randomUUID(), UUID.randomUUID(), "Chest near the floor", ""),
                new StaticImageSpec("https://example.com/top.png")));
    return GenerationRequest.create("push-up", 1, spec, NOW);
  }

  @Test
  void producesOnePictureForEachStage() {
    GenerationRequest r = request();
    assertThat(r.status()).isEqualTo(GenerationStatus.PENDING);
    r.start();
    assertThatThrownBy(() -> r.succeed(List.of("a"), NOW))
        .isInstanceOf(IllegalArgumentException.class);
    r.succeed(List.of("a", "b"), NOW);
    assertThat(r.status()).isEqualTo(GenerationStatus.SUCCEEDED);
    assertThat(r.resultUrls()).containsExactly("a", "b");
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
  void stageSpecsValidateTheirOwnFields() {
    var avatar = UUID.randomUUID();
    var viewpoint = UUID.randomUUID();
    assertThatThrownBy(() -> new AvatarImageSpec(avatar, viewpoint, " ", null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new AvatarImageSpec(null, viewpoint, "x", null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new StaticImageSpec(" ")).isInstanceOf(IllegalArgumentException.class);
    assertThat(new AvatarImageSpec(avatar, viewpoint, "", "https://example.com/r.png")).isNotNull();
    assertThatThrownBy(() -> new IllustrationsGenerationSpec(List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
