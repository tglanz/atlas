package com.tglanz.atlas.backend.graph.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface EdgeJpaRepository extends JpaRepository<EdgeEntity, EdgeEntity.EdgeId> {

  @Query(
      "select e from EdgeEntity e where e.id.sourceKey = :key order by e.id.edgeType, e.id.targetKey")
  List<EdgeEntity> findFrom(@Param("key") String key);

  @Query(
      "select e from EdgeEntity e where e.id.targetKey = :key order by e.id.edgeType, e.id.sourceKey")
  List<EdgeEntity> findTo(@Param("key") String key);

  @Modifying
  @Query("delete from EdgeEntity e where e.id.sourceKey = :key")
  void deleteFrom(@Param("key") String key);
}
