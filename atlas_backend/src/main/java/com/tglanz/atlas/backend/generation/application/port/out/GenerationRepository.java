package com.tglanz.atlas.backend.generation.application.port.out;

import com.tglanz.atlas.backend.generation.domain.GenerationRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenerationRepository {
  Optional<GenerationRequest> findById(UUID id);

  /** Newest first. */
  List<GenerationRequest> findByMovementKey(String movementKey);

  void save(GenerationRequest request);
}
