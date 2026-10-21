package com.tglanz.atlas.backend.movement.application.port.in;

import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;

public interface MovementCommandUseCase {

  Movement createDraft(MovementKey key, MovementContent content);

  Movement updateDraft(MovementKey key, MovementContent content);

  /** Opens a new draft from the latest published revision. */
  Movement createRevision(MovementKey key);

  Movement publish(MovementKey key);
}
