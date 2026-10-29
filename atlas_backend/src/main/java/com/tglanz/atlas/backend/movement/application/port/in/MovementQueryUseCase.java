package com.tglanz.atlas.backend.movement.application.port.in;

import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementInstructions;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementName;
import com.tglanz.atlas.backend.movement.domain.MovementSummary;
import com.tglanz.atlas.backend.movement.domain.TagCount;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface MovementQueryUseCase {

  /**
   * Summaries of the Movements, optionally filtered by name text and tags. With several tags, a
   * Movement must have all of them.
   */
  List<MovementSummary> search(String text, Set<String> tags);

  /** Summaries of the Movements with these keys, by name. Unknown keys are ignored. */
  List<MovementSummary> summaries(Collection<MovementKey> keys);

  /** Current instructions of the Movements with these keys, by name. Unknown keys are ignored. */
  List<MovementInstructions> instructions(Collection<MovementKey> keys);

  /** The name of every Movement, by name. */
  List<MovementName> names();

  /** Every tag in use with the number of Movements having it, by tag. */
  List<TagCount> tags();

  Movement get(MovementKey key);

  boolean exists(MovementKey key);
}
