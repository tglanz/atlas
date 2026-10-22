package com.tglanz.atlas.backend.generation.application.port.in;

import java.util.UUID;

public interface RunGenerationUseCase {

  /** Generates the images of a pending generation, stopping at the first failure. */
  void run(UUID generationId);

  /** Fails what a restart left in progress, as nothing is running it anymore. */
  void failInterrupted();
}
