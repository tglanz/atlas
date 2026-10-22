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
  void aProgressionAlsoAddsTheRegressionTheOtherWay() {
    graph.setOutgoing("push-up", List.of(new Edge("push-up", "pseudo-planche", "progression")));
    assertThat(edges.stored)
        .containsExactlyInAnyOrder(
            new Edge("push-up", "pseudo-planche", "progression"),
            new Edge("pseudo-planche", "push-up", "regression"));
  }

  @Test
  void aRegressionAlsoAddsTheProgressionTheOtherWay() {
    graph.setOutgoing(
        "pseudo-planche", List.of(new Edge("pseudo-planche", "push-up", "regression")));
    assertThat(edges.stored)
        .containsExactlyInAnyOrder(
            new Edge("pseudo-planche", "push-up", "regression"),
            new Edge("push-up", "pseudo-planche", "progression"));
  }

  @Test
  void aTypeThatIsItsOwnInverseGivesTwoEdgesOfTheSameType() {
    graph.setOutgoing("pull-up", List.of(new Edge("pull-up", "supinated", "variant")));
    assertThat(edges.stored)
        .containsExactlyInAnyOrder(
            new Edge("pull-up", "supinated", "variant"),
            new Edge("supinated", "pull-up", "variant"));
  }

  @Test
  void removingARelationshipRemovesItsInverseToo() {
    graph.setOutgoing("push-up", List.of(new Edge("push-up", "pseudo-planche", "progression")));
    graph.setOutgoing("push-up", List.of());
    assertThat(edges.stored).isEmpty();
  }

  @Test
  void theOtherEndSeesTheInverseAndCanRemoveTheRelationship() {
    graph.setOutgoing("push-up", List.of(new Edge("push-up", "pseudo-planche", "progression")));
    assertThat(graph.edgesOf("pseudo-planche").outgoing())
        .containsExactly(new Edge("pseudo-planche", "push-up", "regression"));
    graph.setOutgoing("pseudo-planche", List.of());
    assertThat(edges.stored).isEmpty();
  }

  @Test
  void savingTheSameEdgesAgainChangesNothing() {
    var progression = new Edge("push-up", "pseudo-planche", "progression");
    graph.setOutgoing("push-up", List.of(progression));
    graph.setOutgoing("push-up", List.of(progression));
    assertThat(edges.stored).hasSize(2);
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
  void offersTheKnownTypesWithTheirInverses() {
    assertThat(graph.edgeTypes()).extracting(t -> t.name()).contains("progression", "variant");
    assertThat(graph.edgeTypes())
        .filteredOn(t -> t.name().equals("progression"))
        .extracting(t -> t.inverse())
        .containsExactly("regression");
  }
}
