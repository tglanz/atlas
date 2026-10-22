package com.tglanz.atlas.backend.generation.application.port.out;

import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenerationRepository {
  Optional<GenerationMovementIllustrations> findById(UUID id);

  /** Newest first. */
  List<GenerationMovementIllustrations> findByMovementKey(String movementKey);

  boolean existsInProgress(String movementKey);

  List<GenerationMovementIllustrations> findInProgress();

  /** Throws ConflictException when the Movement already has a generation in progress. */
  void save(GenerationMovementIllustrations generation);
}
