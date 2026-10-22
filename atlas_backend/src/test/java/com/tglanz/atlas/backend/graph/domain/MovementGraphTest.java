package com.tglanz.atlas.backend.graph.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MovementGraphTest {

  // push-up -progression-> pseudo-planche -progression-> planche; pull-up <-variant-> supinated
  private final MovementGraph graph =
      new MovementGraph(
          List.of(
              new Edge("push-up", "pseudo-planche", "Progression"),
              new Edge("pseudo-planche", "planche", "progression"),
              new Edge("planche", "push-up", "regression"),
              new Edge("pull-up", "supinated-pull-up", "variant"),
              new Edge("supinated-pull-up", "pull-up", "variant")));

  private static List<String> keys(List<Reach> reached) {
    return reached.stream().map(Reach::movement).toList();
  }

  @Test
  void edgesAreDirectedTypedAndNormalized() {
    assertThat(new Edge("a", "b", " Progression ").type()).isEqualTo("progression");
    assertThatThrownBy(() -> new Edge("a", "a", "x")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Edge("a", "b", " ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void reportsEdgesLeavingAndEntering() {
    assertThat(graph.outgoing("push-up")).hasSize(1);
    assertThat(graph.incoming("push-up")).extracting(Edge::source).containsExactly("planche");
  }

  @Test
  void traversalGoesNearestFirstAndRespectsDepth() {
    assertThat(keys(graph.traverse("push-up", Direction.OUTGOING, Set.of(), 1)))
        .containsExactly("pseudo-planche");
    assertThat(keys(graph.traverse("push-up", Direction.OUTGOING, Set.of(), 2)))
        .containsExactly("pseudo-planche", "planche");
  }

  @Test
  void traversalVisitsEachMovementOnceDespiteCycles() {
    assertThat(keys(graph.traverse("push-up", Direction.OUTGOING, Set.of(), 10)))
        .containsExactly("pseudo-planche", "planche");
  }

  @Test
  void traversalCanFollowIncomingEdgesOrBoth() {
    assertThat(keys(graph.traverse("push-up", Direction.INCOMING, Set.of(), 1)))
        .containsExactly("planche");
    assertThat(keys(graph.traverse("push-up", Direction.BOTH, Set.of(), 1)))
        .containsExactlyInAnyOrder("pseudo-planche", "planche");
  }

  @Test
  void traversalCanBeLimitedToEdgeTypes() {
    assertThat(keys(graph.traverse("push-up", Direction.OUTGOING, Set.of("regression"), 5)))
        .isEmpty();
    assertThat(keys(graph.traverse("pull-up", Direction.OUTGOING, Set.of("variant"), 5)))
        .containsExactly("supinated-pull-up");
  }

  @Test
  void eachReachRecordsTheEdgeItWasReachedBy() {
    Reach reach = graph.traverse("push-up", Direction.OUTGOING, Set.of(), 1).get(0);
    assertThat(reach.depth()).isEqualTo(1);
    assertThat(reach.via()).isEqualTo(new Edge("push-up", "pseudo-planche", "progression"));
  }
}
