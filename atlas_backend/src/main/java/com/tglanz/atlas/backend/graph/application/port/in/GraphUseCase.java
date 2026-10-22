package com.tglanz.atlas.backend.graph.application.port.in;

import com.tglanz.atlas.backend.graph.domain.Direction;
import com.tglanz.atlas.backend.graph.domain.Edge;
import com.tglanz.atlas.backend.graph.domain.EdgeType;
import com.tglanz.atlas.backend.graph.domain.MovementEdges;
import com.tglanz.atlas.backend.graph.domain.Reach;
import java.util.List;
import java.util.Set;

public interface GraphUseCase {

  /** The edges leaving and entering a Movement. */
  MovementEdges edgesOf(String movement);

  /** The relationship types the user interface offers. */
  List<EdgeType> edgeTypes();

  /**
   * Replaces all edges leaving a Movement. Every edge must start at that Movement and end at an
   * existing one. For a bidirectional type the opposite edge is added or removed too.
   */
  void setOutgoing(String movement, List<Edge> edges);

  /** Movements reachable from the start, nearest first. See {@code MovementGraph.traverse}. */
  List<Reach> traverse(String start, Direction direction, Set<String> types, int maxDepth);
}
