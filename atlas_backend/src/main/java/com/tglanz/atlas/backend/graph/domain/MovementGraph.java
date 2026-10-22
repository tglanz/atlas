package com.tglanz.atlas.backend.graph.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/** The graph of Movements and the edges between them. Immutable; built from a set of edges. */
public final class MovementGraph {
  private final Map<String, List<Edge>> outgoing = new HashMap<>();
  private final Map<String, List<Edge>> incoming = new HashMap<>();

  public MovementGraph(Collection<Edge> edges) {
    for (Edge edge : edges) {
      outgoing.computeIfAbsent(edge.source(), k -> new ArrayList<>()).add(edge);
      incoming.computeIfAbsent(edge.target(), k -> new ArrayList<>()).add(edge);
    }
  }

  public List<Edge> outgoing(String movement) {
    return List.copyOf(outgoing.getOrDefault(movement, List.of()));
  }

  public List<Edge> incoming(String movement) {
    return List.copyOf(incoming.getOrDefault(movement, List.of()));
  }

  /**
   * Breadth-first traversal from a start Movement. Returns every other Movement reachable within
   * maxDepth steps, nearest first, each once, with the edge it was first reached by. An empty type
   * set follows every type.
   */
  public List<Reach> traverse(String start, Direction direction, Set<String> types, int maxDepth) {
    List<Reach> reached = new ArrayList<>();
    Set<String> visited = new HashSet<>(Set.of(start));
    Queue<Reach> queue = new ArrayDeque<>();
    queue.add(new Reach(start, 0, null));
    while (!queue.isEmpty()) {
      Reach current = queue.remove();
      if (current.depth() == maxDepth) {
        continue;
      }
      for (Edge edge : edgesToFollow(current.movement(), direction, types)) {
        String next = edge.source().equals(current.movement()) ? edge.target() : edge.source();
        if (visited.add(next)) {
          Reach reach = new Reach(next, current.depth() + 1, edge);
          reached.add(reach);
          queue.add(reach);
        }
      }
    }
    return reached;
  }

  private List<Edge> edgesToFollow(String movement, Direction direction, Set<String> types) {
    List<Edge> edges = new ArrayList<>();
    if (direction != Direction.INCOMING) {
      edges.addAll(outgoing.getOrDefault(movement, List.of()));
    }
    if (direction != Direction.OUTGOING) {
      edges.addAll(incoming.getOrDefault(movement, List.of()));
    }
    return types.isEmpty() ? edges : edges.stream().filter(e -> types.contains(e.type())).toList();
  }
}
