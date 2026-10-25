package com.tglanz.atlas.backend.graph.application.port.out;

import com.tglanz.atlas.backend.graph.domain.RelatedMovement;
import java.util.Collection;
import java.util.List;

public interface MovementCatalog {
  boolean exists(String movement);

  /** What is shown of each of these Movements. Unknown keys are ignored. */
  List<RelatedMovement> find(Collection<String> movements);
}
