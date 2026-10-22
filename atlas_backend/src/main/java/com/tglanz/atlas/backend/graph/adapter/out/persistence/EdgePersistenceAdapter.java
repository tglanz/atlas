package com.tglanz.atlas.backend.graph.adapter.out.persistence;

import com.tglanz.atlas.backend.graph.application.port.out.EdgeRepository;
import com.tglanz.atlas.backend.graph.domain.Edge;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class EdgePersistenceAdapter implements EdgeRepository {

  private final EdgeJpaRepository jpa;

  EdgePersistenceAdapter(EdgeJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public List<Edge> findAll() {
    return jpa.findAll().stream().map(EdgePersistenceAdapter::toDomain).toList();
  }

  @Override
  public List<Edge> findFrom(String movement) {
    return jpa.findFrom(movement).stream().map(EdgePersistenceAdapter::toDomain).toList();
  }

  @Override
  public List<Edge> findTo(String movement) {
    return jpa.findTo(movement).stream().map(EdgePersistenceAdapter::toDomain).toList();
  }

  @Override
  public void add(Collection<Edge> edges) {
    for (Edge edge : edges) {
      var id = toId(edge);
      if (!jpa.existsById(id)) {
        var entity = new EdgeEntity();
        entity.id = id;
        jpa.save(entity);
      }
    }
    jpa.flush();
  }

  @Override
  public void remove(Collection<Edge> edges) {
    jpa.deleteAllById(edges.stream().map(EdgePersistenceAdapter::toId).toList());
    jpa.flush();
  }

  private static EdgeEntity.EdgeId toId(Edge edge) {
    return new EdgeEntity.EdgeId(edge.source(), edge.target(), edge.type());
  }

  private static Edge toDomain(EdgeEntity e) {
    return new Edge(e.id.sourceKey, e.id.targetKey, e.id.edgeType);
  }
}
