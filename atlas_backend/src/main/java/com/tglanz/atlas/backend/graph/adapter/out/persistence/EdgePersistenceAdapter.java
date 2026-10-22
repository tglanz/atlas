package com.tglanz.atlas.backend.graph.adapter.out.persistence;

import com.tglanz.atlas.backend.graph.application.port.out.EdgeRepository;
import com.tglanz.atlas.backend.graph.domain.Edge;
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
  public void replaceOutgoing(String movement, List<Edge> edges) {
    jpa.deleteFrom(movement);
    jpa.flush();
    for (Edge edge : edges) {
      var entity = new EdgeEntity();
      entity.id = new EdgeEntity.EdgeId(edge.source(), edge.target(), edge.type());
      jpa.save(entity);
    }
    jpa.flush();
  }

  private static Edge toDomain(EdgeEntity e) {
    return new Edge(e.id.sourceKey, e.id.targetKey, e.id.edgeType);
  }
}
