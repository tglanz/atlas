package com.tglanz.atlas.backend.generation.adapter.out.scheduling;

import com.tglanz.atlas.backend.generation.application.port.in.RunGenerationUseCase;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationScheduler;
import java.util.UUID;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
class TaskExecutorGenerationScheduler implements GenerationScheduler {

  private final TaskExecutor executor;
  private final RunGenerationUseCase runner;

  TaskExecutorGenerationScheduler(
      TaskExecutor applicationTaskExecutor, RunGenerationUseCase runner) {
    this.executor = applicationTaskExecutor;
    this.runner = runner;
  }

  @Override
  public void schedule(UUID requestId) {
    Runnable job = () -> executor.execute(() -> runner.run(requestId));
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              job.run();
            }
          });
    } else {
      job.run();
    }
  }
}
