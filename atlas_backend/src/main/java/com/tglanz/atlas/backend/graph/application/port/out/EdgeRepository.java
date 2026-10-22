package com.tglanz.atlas.backend.graph.application.port.out;

import com.tglanz.atlas.backend.graph.domain.Edge;
import java.util.Collection;
import java.util.List;

public interface EdgeRepository {
  List<Edge> findAll();

  List<Edge> findFrom(String movement);

  List<Edge> findTo(String movement);

  /** Adds the edges that are not there yet. */
  void add(Collection<Edge> edges);

  void remove(Collection<Edge> edges);
}
