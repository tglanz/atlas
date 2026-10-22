package com.tglanz.atlas.backend.generation.application.port.in;

import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations;
import java.util.List;
import java.util.UUID;

public interface GenerationUseCase {

  /** Queues the generation of one image for each stage of the Movement. */
  GenerationMovementIllustrations request(String movementKey, UUID avatarId, UUID viewpointId);

  GenerationMovementIllustrations get(String movementKey, UUID id);

  List<GenerationMovementIllustrations> list(String movementKey);

  /** Puts the images on the Movement, replacing its Illustrations. */
  GenerationMovementIllustrations apply(String movementKey, UUID id);

  void discard(String movementKey, UUID id);
}
