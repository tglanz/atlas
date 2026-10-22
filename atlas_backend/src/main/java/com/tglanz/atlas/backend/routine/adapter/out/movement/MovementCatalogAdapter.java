package com.tglanz.atlas.backend.routine.adapter.out.movement;

import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.routine.application.port.out.MovementCatalog;
import org.springframework.stereotype.Component;

/** Checks Movements through the movement module's public use cases. */
@Component
class MovementCatalogAdapter implements MovementCatalog {

  private final MovementQueryUseCase movements;

  MovementCatalogAdapter(MovementQueryUseCase movements) {
    this.movements = movements;
  }

  @Override
  public boolean exists(String movementKey) {
    try {
      return movements.exists(new MovementKey(movementKey));
    } catch (IllegalArgumentException e) {
      return false; // not a valid key, so no such Movement
    }
  }
}
