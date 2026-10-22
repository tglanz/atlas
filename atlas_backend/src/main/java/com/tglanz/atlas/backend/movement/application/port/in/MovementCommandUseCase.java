package com.tglanz.atlas.backend.movement.application.port.in;

import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import java.util.List;

public interface MovementCommandUseCase {

  Movement createDraft(MovementKey key, MovementContent content);

  Movement updateDraft(MovementKey key, MovementContent content);

  /** Opens a new draft from the latest published revision. */
  Movement createRevision(MovementKey key);

  Movement publish(MovementKey key);

  /** Appends an Illustration to the end of the open draft's sequence. */
  Movement addIllustration(MovementKey key, Illustration illustration);

  /** Replaces the open draft's sequence. The open draft must be the expected revision. */
  Movement replaceIllustrations(
      MovementKey key, int expectedRevision, List<Illustration> illustrations);

  /** Removes all Illustrations from the open draft. */
  Movement clearIllustrations(MovementKey key);
}
