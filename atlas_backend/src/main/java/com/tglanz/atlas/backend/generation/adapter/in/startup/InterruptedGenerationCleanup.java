package com.tglanz.atlas.backend.generation.adapter.in.startup;

import com.tglanz.atlas.backend.generation.application.port.in.RunGenerationUseCase;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Generations left in progress by a restart would block their Movements for good. */
@Component
class InterruptedGenerationCleanup implements ApplicationRunner {

  private final RunGenerationUseCase worker;

  InterruptedGenerationCleanup(RunGenerationUseCase worker) {
    this.worker = worker;
  }

  @Override
  public void run(ApplicationArguments args) {
    worker.failInterrupted();
  }
}
