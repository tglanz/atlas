package com.tglanz.atlas.backend.movement.application.port.out;

import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import java.util.List;
import java.util.Optional;

public interface MovementRepository {

  boolean existsKey(MovementKey key);

  Optional<Movement> find(MovementKey key, int revision);

  Optional<Movement> findLatestPublished(MovementKey key);

  Optional<Movement> findDraft(MovementKey key);

  /** All revisions of a Movement, oldest first. */
  List<Movement> findAllRevisions(MovementKey key);

  /** Latest published revision of every Movement matching the filters; blank means no filter. */
  List<Movement> searchCurrentPublished(String text, String tag);

  void save(Movement movement);
}
