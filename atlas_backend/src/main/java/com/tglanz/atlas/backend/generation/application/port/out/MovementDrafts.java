package com.tglanz.atlas.backend.generation.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovementDrafts {

  record Draft(int revision, String name, String description) {}

  /** The open draft of the Movement, if any. */
  Optional<Draft> findDraft(String movementKey);

  void replaceIllustrations(String movementKey, int revision, UUID avatarId, List<String> urls);

  void addIllustration(String movementKey, UUID avatarId, String url);
}
