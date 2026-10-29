package com.tglanz.atlas.backend.routine.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface RoutineJpaRepository extends JpaRepository<RoutineEntity, String> {
  List<RoutineEntity> findByOwnerIdOrderByNameAsc(String ownerId);

  Optional<RoutineEntity> findByShareToken(String shareToken);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      "update RoutineEntity r set r.shareToken = :token where r.id = :id and r.shareToken is null")
  int enableSharing(@Param("id") String id, @Param("token") String token);

  @Query("select r.shareToken from RoutineEntity r where r.id = :id")
  Optional<String> shareToken(@Param("id") String id);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("update RoutineEntity r set r.shareToken = null where r.id = :id")
  int disableSharing(@Param("id") String id);
}
