package com.tglanz.atlas.backend.movement.application.port.out;

import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import java.util.List;
import java.util.Optional;

public interface MovementRepository {

  boolean exists(MovementKey key);

  Optional<Movement> find(MovementKey key);

  /** Movements matching the filters, by name; blank means no filter. */
  List<Movement> search(String text, String tag);

  void save(Movement movement);
}
