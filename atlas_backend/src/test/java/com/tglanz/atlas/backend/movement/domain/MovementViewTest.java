package com.tglanz.atlas.backend.movement.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MovementViewTest {

  private final UUID sketch = UUID.randomUUID();
  private final UUID cartoon = UUID.randomUUID();

  private Movement movement(Map<UUID, List<Illustration>> illustrations) {
    return Movement.restore(
        new MovementKey("push-up"),
        1,
        RevisionStatus.PUBLISHED,
        Instant.parse("2026-01-01T00:00:00Z"),
        new MovementContent("Push-up", "", List.of(), java.util.Set.of(), List.of()),
        illustrations);
  }

  @Test
  void usesPreferredAvatarWhenItHasIllustrations() {
    var m =
        movement(
            Map.of(
                sketch, List.of(new Illustration("sketch-down"), new Illustration("sketch-up")),
                cartoon, List.of(new Illustration("cartoon-down"))));
    MovementView view = MovementView.of(m, new AvatarChoice(cartoon, sketch));
    assertThat(view.avatarId()).isEqualTo(cartoon);
    assertThat(view.illustrations()).extracting(Illustration::url).containsExactly("cartoon-down");
  }

  @Test
  void fallsBackToDefaultAvatarKeepingOrder() {
    var m = movement(Map.of(sketch, List.of(new Illustration("down"), new Illustration("up"))));
    MovementView view = MovementView.of(m, new AvatarChoice(cartoon, sketch));
    assertThat(view.avatarId()).isEqualTo(sketch);
    assertThat(view.illustrations()).extracting(Illustration::url).containsExactly("down", "up");
  }

  @Test
  void anonymousViewerGetsDefaultAvatar() {
    var m = movement(Map.of(sketch, List.of(new Illustration("down"))));
    assertThat(MovementView.of(m, new AvatarChoice(null, sketch)).avatarId()).isEqualTo(sketch);
  }

  @Test
  void noIllustrationsForEitherAvatarYieldsEmptyView() {
    var m = movement(Map.of());
    MovementView view = MovementView.of(m, new AvatarChoice(cartoon, sketch));
    assertThat(view.avatarId()).isNull();
    assertThat(view.illustrations()).isEmpty();
  }
}
