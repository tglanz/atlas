package com.tglanz.atlas.backend.movement.application.port.in;

import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import java.util.List;

public interface MovementQueryUseCase {

  /** Current published revisions, optionally filtered by name text and tag. */
  List<Movement> search(String text, String tag);

  Movement getCurrent(MovementKey key);

  /** Published revisions of a Movement, oldest first. */
  List<Movement> listPublishedRevisions(MovementKey key);

  Movement getPublishedRevision(MovementKey key, int revision);

  boolean isPublished(MovementKey key, int revision);

  /** The open draft, for maintainers. */
  Movement getDraft(MovementKey key);
}
