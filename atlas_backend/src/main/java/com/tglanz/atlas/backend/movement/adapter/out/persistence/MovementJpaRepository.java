package com.tglanz.atlas.backend.movement.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MovementJpaRepository extends JpaRepository<MovementEntity, String> {

  /**
   * A Movement matches when its name contains the text and it has every one of the tags. The tag
   * count is zero when there are no tags to match, in which case the tag list is ignored.
   */
  @Query(
      """
      select m from MovementEntity m
      where (:text = '' or lower(m.name) like lower(concat('%', :text, '%')))
        and (:tagCount = 0 or (
          select count(distinct t) from MovementEntity other join other.tags t
          where other.movementKey = m.movementKey and t in :tags) = :tagCount)
      order by m.name
      """)
  List<MovementEntity> search(
      @Param("text") String text,
      @Param("tags") Collection<String> tags,
      @Param("tagCount") long tagCount);

  @Query("select m.movementKey, m.name from MovementEntity m order by m.name")
  List<Object[]> findNames();

  @Query("select t, count(m) from MovementEntity m join m.tags t group by t order by t")
  List<Object[]> countTags();
}
