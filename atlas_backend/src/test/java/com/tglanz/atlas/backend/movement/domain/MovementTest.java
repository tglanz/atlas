package com.tglanz.atlas.backend.movement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MovementTest {

  private static MovementContent content(String name) {
    return new MovementContent(
        name, "desc", List.of(new MovementItem(ItemType.CUE, "Brace")), Set.of("Core"));
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
    assertThat(new MovementKey("supinated_pullup").value()).isEqualTo("supinated_pullup");
    assertThatThrownBy(() -> new MovementKey("a_-b")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void contentRequiresNameAndNormalizesTags() {
    assertThatThrownBy(() -> content(" ")).isInstanceOf(IllegalArgumentException.class);
    assertThat(content("Plank").tags()).containsExactly("core");
  }

  @Test
  void itemsHaveATypeAndContent() {
    assertThatThrownBy(() -> new MovementItem(ItemType.COMMENT, " "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new MovementItem(null, "x"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(new MovementItem(ItemType.COMMENT, " Mind the wrist ").content())
        .isEqualTo("Mind the wrist");
  }
}
