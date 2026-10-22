package com.tglanz.atlas.backend.movement.application.port.in;

import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementName;
import com.tglanz.atlas.backend.movement.domain.TagCount;
import java.util.List;
import java.util.Set;

public interface MovementQueryUseCase {

  /**
   * Movements, optionally filtered by name text and tags. With several tags, a Movement must have
   * all of them.
   */
  List<Movement> search(String text, Set<String> tags);

  /** The name of every Movement, by name. */
  List<MovementName> names();

  /** Every tag in use with the number of Movements having it, by tag. */
  List<TagCount> tags();

  Movement get(MovementKey key);

  boolean exists(MovementKey key);
}
