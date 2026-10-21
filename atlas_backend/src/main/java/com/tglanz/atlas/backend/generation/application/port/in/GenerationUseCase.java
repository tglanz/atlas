package com.tglanz.atlas.backend.generation.application.port.in;

import com.tglanz.atlas.backend.generation.domain.GenerationRequest;
import com.tglanz.atlas.backend.generation.domain.Stage;
import java.util.List;
import java.util.UUID;

public interface GenerationUseCase {

  /** Queues generation of an Avatar's Illustration sequence for the Movement's open draft. */
  GenerationRequest request(
      String movementKey, UUID avatarId, UUID viewpointId, List<Stage> stages);

  GenerationRequest get(String movementKey, UUID id);

  List<GenerationRequest> list(String movementKey);

  GenerationRequest retry(String movementKey, UUID id);
}
