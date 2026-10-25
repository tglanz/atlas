package com.tglanz.atlas.backend.graph.adapter.out.movement;

import com.tglanz.atlas.backend.graph.application.port.out.MovementCatalog;
import com.tglanz.atlas.backend.graph.domain.RelatedMovement;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;

/** Checks Movements through the movement module's public use cases. */
@Component
class GraphMovementCatalogAdapter implements MovementCatalog {

  private final MovementQueryUseCase movements;

  GraphMovementCatalogAdapter(MovementQueryUseCase movements) {
    this.movements = movements;
  }

  @Override
  public List<RelatedMovement> find(Collection<String> keys) {
    return movements.summaries(keys.stream().map(MovementKey::new).toList()).stream()
        .map(
            s ->
                new RelatedMovement(
                    s.key().value(), s.name(), s.tags(), s.representativeIllustration()))
        .toList();
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
