package com.tglanz.atlas.backend.graph.application.port.out;

import com.tglanz.atlas.backend.graph.domain.Edge;
import java.util.List;

public interface EdgeRepository {
  List<Edge> findAll();

  List<Edge> findFrom(String movement);

  List<Edge> findTo(String movement);

  /** Replaces every edge leaving the Movement with the given ones. */
  void replaceOutgoing(String movement, List<Edge> edges);
}
