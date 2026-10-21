package com.tglanz.atlas.backend.generation.application.port.out;

import java.util.UUID;

public interface GenerationScheduler {

  /** Arranges for the request to run in the background, once the current transaction commits. */
  void schedule(UUID requestId);
}
