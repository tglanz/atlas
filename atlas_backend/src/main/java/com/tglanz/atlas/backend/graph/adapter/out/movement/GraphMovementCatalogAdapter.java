package com.tglanz.atlas.backend.graph.adapter.out.movement;

import com.tglanz.atlas.backend.graph.application.port.out.MovementCatalog;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import org.springframework.stereotype.Component;

/** Checks Movements through the movement module's public use cases. */
@Component
class GraphMovementCatalogAdapter implements MovementCatalog {

  private final MovementQueryUseCase movements;

  GraphMovementCatalogAdapter(MovementQueryUseCase movements) {
    this.movements = movements;
  }

  @Override
  public boolean exists(String movement) {
    try {
      return movements.exists(new MovementKey(movement));
    } catch (IllegalArgumentException e) {
      return false; // not a valid key, so no such Movement
    }
  }
}
