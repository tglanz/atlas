package com.tglanz.atlas.backend.movement.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MovementJpaRepository extends JpaRepository<MovementEntity, String> {

  @Query(
      """
      select m from MovementEntity m
      where (:text = '' or lower(m.name) like lower(concat('%', :text, '%')))
        and (:tag = '' or :tag member of m.tags)
      order by m.name
      """)
  List<MovementEntity> search(@Param("text") String text, @Param("tag") String tag);
}
