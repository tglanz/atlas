package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.out.GenerationRepository;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The short transactions around a generation run. Slow provider calls happen between them, so no
 * database transaction is held open while waiting on the provider.
 */
@Service
public class GenerationLifecycle {

  private final GenerationRepository generations;

  public GenerationLifecycle(GenerationRepository generations) {
    this.generations = generations;
  }

  @Transactional(readOnly = true)
  public Optional<GenerationMovementIllustrations> find(UUID id) {
    return generations.findById(id);
  }

  /** Loads, changes and saves the generation in one transaction. */
  @Transactional
  public GenerationMovementIllustrations update(
      UUID id, Consumer<GenerationMovementIllustrations> change) {
    GenerationMovementIllustrations generation = generations.findById(id).orElseThrow();
    change.accept(generation);
    generations.save(generation);
    return generation;
  }

  @Transactional(readOnly = true)
  public List<GenerationMovementIllustrations> findInProgress() {
    return generations.findInProgress();
  }
}
