package com.tglanz.atlas.backend.routine.adapter.out.movement;

import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.routine.application.port.out.PublishedMovementCatalog;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import org.springframework.stereotype.Component;

/** Checks Movement references through the movement module's public use cases. */
@Component
class MovementCatalogAdapter implements PublishedMovementCatalog {

  private final MovementQueryUseCase movements;

  MovementCatalogAdapter(MovementQueryUseCase movements) {
    this.movements = movements;
  }

  @Override
  public boolean isPublished(String key, int revision) {
    try {
      movements.getPublishedRevision(new MovementKey(key), revision);
      return true;
    } catch (NotFoundException e) {
      return false;
    }
  }
}
