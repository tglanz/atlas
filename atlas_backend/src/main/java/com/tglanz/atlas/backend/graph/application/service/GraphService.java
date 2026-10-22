package com.tglanz.atlas.backend.graph.application.service;

import com.tglanz.atlas.backend.graph.application.port.in.GraphUseCase;
import com.tglanz.atlas.backend.graph.application.port.out.EdgeRepository;
import com.tglanz.atlas.backend.graph.application.port.out.MovementCatalog;
import com.tglanz.atlas.backend.graph.domain.Direction;
import com.tglanz.atlas.backend.graph.domain.Edge;
import com.tglanz.atlas.backend.graph.domain.MovementEdges;
import com.tglanz.atlas.backend.graph.domain.MovementGraph;
import com.tglanz.atlas.backend.graph.domain.Reach;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GraphService implements GraphUseCase {

  private final EdgeRepository edges;
  private final MovementCatalog movements;

  public GraphService(EdgeRepository edges, MovementCatalog movements) {
    this.edges = edges;
    this.movements = movements;
  }

  @Override
  @Transactional(readOnly = true)
  public MovementEdges edgesOf(String movement) {
    requireExists(movement);
    return new MovementEdges(edges.findFrom(movement), edges.findTo(movement));
  }

  @Override
  public void setOutgoing(String movement, List<Edge> newEdges) {
    requireExists(movement);
    for (Edge edge : newEdges) {
      if (!edge.source().equals(movement)) {
        throw new IllegalArgumentException("Every edge must start at " + movement);
      }
      if (!movements.exists(edge.target())) {
        throw new IllegalArgumentException("Movement " + edge.target() + " does not exist");
      }
    }
    edges.replaceOutgoing(movement, newEdges.stream().distinct().toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Reach> traverse(String start, Direction direction, Set<String> types, int maxDepth) {
    requireExists(start);
    return new MovementGraph(edges.findAll()).traverse(start, direction, types, maxDepth);
  }

  private void requireExists(String movement) {
    if (!movements.exists(movement)) {
      throw new NotFoundException("No movement " + movement);
    }
  }
}
