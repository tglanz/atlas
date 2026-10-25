package com.tglanz.atlas.backend.movement.application.port.out;

import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementName;
import com.tglanz.atlas.backend.movement.domain.MovementSummary;
import com.tglanz.atlas.backend.movement.domain.TagCount;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MovementRepository {

  boolean exists(MovementKey key);

  Optional<Movement> find(MovementKey key);

  /**
   * Summaries of the Movements matching the filters, by name. Blank text and no tags mean no
   * filter.
   */
  List<MovementSummary> search(String text, Set<String> tags);

  /** Summaries of the Movements with these keys, by name. Unknown keys are ignored. */
  List<MovementSummary> findSummaries(Collection<MovementKey> keys);

  List<MovementName> findAllNames();

  List<TagCount> countTags();

  void save(Movement movement);
}
