package com.tglanz.atlas.backend.movement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MovementTest {

  private static MovementContent content(String name) {
    return new MovementContent(name, "desc", List.of("cue"), Set.of("Core"), List.of());
  }

  private static Movement movement() {
    return Movement.create(new MovementKey("plank"), content("Plank"));
  }

  @Test
  void movementsAreEditedInPlace() {
    Movement m = movement();
    m.edit(content("Front plank"));
    assertThat(m.content().name()).isEqualTo("Front plank");
  }

  @Test
  void illustrationsAreAnOrderedSequenceThatCanBeReplaced() {
    Movement m = movement();
    assertThat(m.illustrations()).isEmpty();
    m.replaceIllustrations(List.of(new Illustration("down"), new Illustration("up")));
    assertThat(m.illustrations()).extracting(Illustration::url).containsExactly("down", "up");
    m.replaceIllustrations(List.of());
    assertThat(m.illustrations()).isEmpty();
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
