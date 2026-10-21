package com.tglanz.atlas.backend.movement.adapter.out.persistence;

import com.tglanz.atlas.backend.movement.domain.RevisionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MovementRevisionJpaRepository extends JpaRepository<MovementRevisionEntity, String> {

  boolean existsByMovementKey(String movementKey);

  Optional<MovementRevisionEntity> findByMovementKeyAndRevision(String movementKey, int revision);

  Optional<MovementRevisionEntity> findFirstByMovementKeyAndStatusOrderByRevisionDesc(
      String movementKey, RevisionStatus status);

  List<MovementRevisionEntity> findByMovementKeyOrderByRevisionAsc(String movementKey);

  @Query(
      """
      select m from MovementRevisionEntity m
      where m.status = com.tglanz.atlas.backend.movement.domain.RevisionStatus.PUBLISHED
        and m.revision = (
          select max(p.revision) from MovementRevisionEntity p
          where p.movementKey = m.movementKey
            and p.status = com.tglanz.atlas.backend.movement.domain.RevisionStatus.PUBLISHED)
        and (:text = '' or lower(m.name) like lower(concat('%', :text, '%')))
        and (:tag = '' or :tag member of m.tags)
      order by m.name
      """)
  List<MovementRevisionEntity> searchCurrentPublished(
      @Param("text") String text, @Param("tag") String tag);
}
