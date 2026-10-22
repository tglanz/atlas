package com.tglanz.atlas.backend.generation.application.port.in;

import com.tglanz.atlas.backend.generation.domain.GenerationRequest;
import com.tglanz.atlas.backend.generation.domain.IllustrationsGenerationSpec;
import java.util.List;
import java.util.UUID;

public interface GenerationUseCase {

  /** Queues production of the Movement's Illustration sequence from the spec. */
  GenerationRequest request(String movementKey, IllustrationsGenerationSpec spec);

  GenerationRequest get(String movementKey, UUID id);

  List<GenerationRequest> list(String movementKey);

  GenerationRequest retry(String movementKey, UUID id);
}
