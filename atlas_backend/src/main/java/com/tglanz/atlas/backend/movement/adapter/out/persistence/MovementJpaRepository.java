package com.tglanz.atlas.backend.movement.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MovementJpaRepository extends JpaRepository<MovementEntity, String> {

  /**
   * One row for each tag of each matching Movement (a Movement without tags has one row), with the
   * first Illustration's image. A Movement matches when its name contains the text and it has every
   * one of the tags. The tag count is zero when there are no tags to match, in which case the tag
   * list is ignored.
   */
  @Query(
      """
      select m.movementKey, m.name, t, i.imageUrl
      from MovementEntity m
      left join m.tags t
      left join m.illustrations i on index(i) = 0
      where (:text = '' or lower(m.name) like lower(concat('%', :text, '%')))
        and (:tagCount = 0 or (
          select count(distinct ft) from MovementEntity other join other.tags ft
          where other.movementKey = m.movementKey and ft in :tags) = :tagCount)
      order by m.name, m.movementKey, t
      """)
  List<Object[]> searchSummaryRows(
      @Param("text") String text,
      @Param("tags") Collection<String> tags,
      @Param("tagCount") long tagCount);

  /** The same rows as the search, for the Movements with these keys. */
  @Query(
      """
      select m.movementKey, m.name, t, i.imageUrl
      from MovementEntity m
      left join m.tags t
      left join m.illustrations i on index(i) = 0
      where m.movementKey in :keys
      order by m.name, m.movementKey, t
      """)
  List<Object[]> summaryRowsByKeys(@Param("keys") Collection<String> keys);

  @Query("select m.movementKey, m.name from MovementEntity m order by m.name")
  List<Object[]> findNames();

  @Query("select t, count(m) from MovementEntity m join m.tags t group by t order by t")
  List<Object[]> countTags();
}
