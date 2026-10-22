package com.tglanz.atlas.backend.movement.application.port.in;

import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import java.util.List;

public interface MovementQueryUseCase {

  /** Movements, optionally filtered by name text and tag. */
  List<Movement> search(String text, String tag);

  Movement get(MovementKey key);

  boolean exists(MovementKey key);
}
