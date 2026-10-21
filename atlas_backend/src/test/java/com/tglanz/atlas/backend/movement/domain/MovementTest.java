package com.tglanz.atlas.backend.movement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tglanz.atlas.backend.shared.error.ConflictException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MovementTest {

  private static MovementContent content(String name) {
    return new MovementContent(name, "desc", List.of("cue"), Set.of("Core"), List.of());
  }

  private static Movement draft() {
    return Movement.newDraft(new MovementKey("plank"), content("Plank"));
  }

  @Test
  void newMovementIsDraftRevisionOne() {
    Movement m = draft();
    assertThat(m.revision()).isEqualTo(1);
    assertThat(m.status()).isEqualTo(RevisionStatus.DRAFT);
    assertThat(m.publishedAt()).isNull();
  }

  @Test
  void draftsCanBeEditedInPlace() {
    Movement m = draft();
    m.edit(content("Front plank"));
    assertThat(m.content().name()).isEqualTo("Front plank");
    assertThat(m.revision()).isEqualTo(1);
  }

  @Test
  void publishedRevisionsAreImmutable() {
    Movement m = draft();
    m.publish(Instant.parse("2026-01-01T00:00:00Z"));
    assertThat(m.status()).isEqualTo(RevisionStatus.PUBLISHED);
    assertThatThrownBy(() -> m.edit(content("Other"))).isInstanceOf(ConflictException.class);
    assertThatThrownBy(() -> m.publish(Instant.now())).isInstanceOf(ConflictException.class);
  }

  @Test
  void revisingCreatesNextDraftWithSameKeyAndContent() {
    Movement m = draft();
    m.publish(Instant.now());
    Movement next = m.revise();
    assertThat(next.key()).isEqualTo(m.key());
    assertThat(next.revision()).isEqualTo(2);
    assertThat(next.status()).isEqualTo(RevisionStatus.DRAFT);
    assertThat(next.content()).isEqualTo(m.content());
  }

  @Test
  void draftsCannotBeRevised() {
    assertThatThrownBy(() -> draft().revise()).isInstanceOf(ConflictException.class);
  }

  @Test
  void keyMustBeLowercaseSlug() {
    assertThatThrownBy(() -> new MovementKey("Front Plank"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(new MovementKey("front-plank").value()).isEqualTo("front-plank");
  }

  @Test
  void contentRequiresNameAndNormalizesTags() {
    assertThatThrownBy(() -> content(" ")).isInstanceOf(IllegalArgumentException.class);
    assertThat(content("Plank").tags()).containsExactly("core");
  }
}
