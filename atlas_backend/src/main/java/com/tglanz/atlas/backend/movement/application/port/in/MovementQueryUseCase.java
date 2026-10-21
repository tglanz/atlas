package com.tglanz.atlas.backend.movement.application.port.in;

import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementView;
import java.util.List;

public interface MovementQueryUseCase {

  /**
   * Current published revisions, optionally filtered by name text and tag. The viewer id is null
   * for anonymous visitors and selects which Avatar's Illustrations are shown.
   */
  List<MovementView> search(String text, String tag, String viewerId);

  MovementView getCurrent(MovementKey key, String viewerId);

  /** Published revisions of a Movement, oldest first. */
  List<Movement> listPublishedRevisions(MovementKey key);

  MovementView getPublishedRevision(MovementKey key, int revision, String viewerId);

  boolean isPublished(MovementKey key, int revision);

  /** The open draft with Illustrations of every Avatar, for maintainers. */
  Movement getDraft(MovementKey key);
}
