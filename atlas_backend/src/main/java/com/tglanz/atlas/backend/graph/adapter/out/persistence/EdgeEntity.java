package com.tglanz.atlas.backend.graph.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "movement_edge")
class EdgeEntity {

  @EmbeddedId EdgeId id;

  @Embeddable
  static class EdgeId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "source_key")
    String sourceKey;

    @Column(name = "target_key")
    String targetKey;

    @Column(name = "edge_type")
    String edgeType;

    EdgeId() {}

    EdgeId(String sourceKey, String targetKey, String edgeType) {
      this.sourceKey = sourceKey;
      this.targetKey = targetKey;
      this.edgeType = edgeType;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof EdgeId e
          && sourceKey.equals(e.sourceKey)
          && targetKey.equals(e.targetKey)
          && edgeType.equals(e.edgeType);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(sourceKey, targetKey, edgeType);
    }
  }
}
