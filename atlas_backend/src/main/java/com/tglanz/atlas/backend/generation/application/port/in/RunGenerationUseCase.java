package com.tglanz.atlas.backend.generation.application.port.in;

import java.util.UUID;

public interface RunGenerationUseCase {

  /** Performs a pending generation request. Failures are recorded on the request. */
  void run(UUID requestId);
}
