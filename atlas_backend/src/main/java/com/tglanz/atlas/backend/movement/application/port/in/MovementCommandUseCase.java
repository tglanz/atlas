package com.tglanz.atlas.backend.movement.application.port.in;

import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import java.util.List;
import java.util.UUID;

public interface MovementCommandUseCase {

  Movement createDraft(MovementKey key, MovementContent content);

  Movement updateDraft(MovementKey key, MovementContent content);

  /** Opens a new draft from the latest published revision. */
  Movement createRevision(MovementKey key);

  Movement publish(MovementKey key);

  /** Appends an Illustration to the Avatar's sequence on the open draft. */
  Movement addIllustration(MovementKey key, UUID avatarId, Illustration illustration);

  /** Replaces the Avatar's sequence on the open draft, which must be the expected revision. */
  Movement replaceIllustrations(
      MovementKey key, int expectedRevision, UUID avatarId, List<Illustration> illustrations);

  /** Removes the Avatar's whole sequence from the open draft. */
  Movement clearIllustrations(MovementKey key, UUID avatarId);
}
