package com.tglanz.atlas.backend.graph.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tglanz.atlas.backend.graph.application.port.out.EdgeRepository;
import com.tglanz.atlas.backend.graph.application.service.GraphService;
import com.tglanz.atlas.backend.graph.domain.Edge;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GraphServiceTest {

  /** In-memory stand-in for the database. */
  private static final class FakeEdges implements EdgeRepository {
    final Set<Edge> stored = new LinkedHashSet<>();

    public List<Edge> findAll() {
      return List.copyOf(stored);
    }

    public List<Edge> findFrom(String movement) {
      return stored.stream().filter(e -> e.source().equals(movement)).toList();
    }

    public List<Edge> findTo(String movement) {
      return stored.stream().filter(e -> e.target().equals(movement)).toList();
    }

    public void add(Collection<Edge> edges) {
      stored.addAll(edges);
    }

    public void remove(Collection<Edge> edges) {
      stored.removeAll(edges);
    }
  }

  private final FakeEdges edges = new FakeEdges();
  private final Set<String> existing = Set.of("push-up", "pseudo-planche", "pull-up", "supinated");
  private final GraphService graph = new GraphService(edges, existing::contains);

  @Test
  void aOneWayTypeStoresOneEdge() {
    graph.setOutgoing("push-up", List.of(new Edge("push-up", "pseudo-planche", "progression")));
    assertThat(edges.stored).containsExactly(new Edge("push-up", "pseudo-planche", "progression"));
  }

  @Test
  void aBidirectionalTypeStoresBothDirections() {
    graph.setOutgoing("pull-up", List.of(new Edge("pull-up", "supinated", "variant")));
    assertThat(edges.stored)
        .containsExactlyInAnyOrder(
            new Edge("pull-up", "supinated", "variant"),
            new Edge("supinated", "pull-up", "variant"));
  }

  @Test
  void removingABidirectionalEdgeRemovesTheOtherDirectionToo() {
    graph.setOutgoing("pull-up", List.of(new Edge("pull-up", "supinated", "variant")));
    graph.setOutgoing("pull-up", List.of());
    assertThat(edges.stored).isEmpty();
  }

  @Test
  void savingTheSameEdgesAgainChangesNothing() {
    var variant = new Edge("pull-up", "supinated", "variant");
    graph.setOutgoing("pull-up", List.of(variant));
    graph.setOutgoing("pull-up", List.of(variant));
    assertThat(edges.stored).hasSize(2);
  }

  @Test
  void theOtherEndSeesAndCanRemoveTheMirror() {
    graph.setOutgoing("pull-up", List.of(new Edge("pull-up", "supinated", "variant")));
    assertThat(graph.edgesOf("supinated").outgoing()).hasSize(1);
    graph.setOutgoing("supinated", List.of());
    assertThat(edges.stored).isEmpty();
  }

  @Test
  void unknownTypesAreAcceptedAsOneWay() {
    graph.setOutgoing("push-up", List.of(new Edge("push-up", "pull-up", "my own type")));
    assertThat(edges.stored).containsExactly(new Edge("push-up", "pull-up", "my own type"));
  }

  @Test
  void edgesMustStartAtTheMovementAndEndAtAnExistingOne() {
    assertThatThrownBy(
            () ->
                graph.setOutgoing("push-up", List.of(new Edge("pull-up", "supinated", "variant"))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> graph.setOutgoing("push-up", List.of(new Edge("push-up", "nope", "variant"))))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void offersTheKnownTypes() {
    assertThat(graph.edgeTypes()).extracting(t -> t.name()).contains("progression", "variant");
  }
}
