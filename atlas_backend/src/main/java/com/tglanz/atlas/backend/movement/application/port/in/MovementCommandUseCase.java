package com.tglanz.atlas.backend.movement.application.port.in;

import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import java.util.List;

public interface MovementCommandUseCase {

  Movement create(MovementKey key, MovementContent content);

  Movement update(MovementKey key, MovementContent content);

  /** Replaces the Movement's whole sequence of Illustrations; empty removes them all. */
  Movement replaceIllustrations(MovementKey key, List<Illustration> illustrations);
}
