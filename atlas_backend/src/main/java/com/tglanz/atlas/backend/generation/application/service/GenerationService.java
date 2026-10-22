package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.in.GenerationUseCase;
import com.tglanz.atlas.backend.generation.application.port.out.AvatarSpecifications;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationRepository;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationScheduler;
import com.tglanz.atlas.backend.generation.application.port.out.Movements;
import com.tglanz.atlas.backend.generation.application.port.out.ViewpointRepository;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations.StageDefinition;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GenerationService implements GenerationUseCase {

  private static final Logger log = LoggerFactory.getLogger(GenerationService.class);

  private final GenerationRepository generations;
  private final Movements movements;
  private final AvatarSpecifications avatars;
  private final ViewpointRepository viewpoints;
  private final GenerationScheduler scheduler;
  private final Clock clock;

  public GenerationService(
      GenerationRepository generations,
      Movements movements,
      AvatarSpecifications avatars,
      ViewpointRepository viewpoints,
      GenerationScheduler scheduler,
      Clock clock) {
    this.generations = generations;
    this.movements = movements;
    this.avatars = avatars;
    this.viewpoints = viewpoints;
    this.scheduler = scheduler;
    this.clock = clock;
  }

  @Override
  public GenerationMovementIllustrations request(String movementKey, List<StageDefinition> stages) {
    if (movements.find(movementKey).isEmpty()) {
      throw new NotFoundException("No movement " + movementKey);
    }
    for (StageDefinition stage : stages) {
      if (avatars.find(stage.avatarId()).isEmpty()) {
        throw new IllegalArgumentException("Unknown avatar");
      }
      if (viewpoints.findById(stage.viewpointId()).isEmpty()) {
        throw new IllegalArgumentException("Unknown viewpoint");
      }
    }
    if (generations.existsInProgress(movementKey)) {
      throw new ConflictException("This movement already has a generation in progress");
    }
    var generation = GenerationMovementIllustrations.create(movementKey, stages, clock.instant());
    generations.save(generation);
    scheduler.schedule(generation.id());
    log.info(
        "Generation {} requested for movement {} with {} stages",
        generation.id(),
        movementKey,
        stages.size());
    return generation;
  }

  @Override
  @Transactional(readOnly = true)
  public GenerationMovementIllustrations get(String movementKey, UUID id) {
    return generations
        .findById(id)
        .filter(g -> g.movementKey().equals(movementKey))
        .orElseThrow(() -> new NotFoundException("Generation not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public List<GenerationMovementIllustrations> list(String movementKey) {
    return generations.findByMovementKey(movementKey);
  }

  @Override
  public GenerationMovementIllustrations apply(String movementKey, UUID id) {
    GenerationMovementIllustrations generation = get(movementKey, id);
    if (movements.find(movementKey).isEmpty()) {
      throw new NotFoundException("No movement " + movementKey);
    }
    movements.replaceIllustrations(movementKey, generation.approve());
    generations.save(generation);
    log.info("Generation {} applied to movement {}", id, movementKey);
    return generation;
  }

  @Override
  public void discard(String movementKey, UUID id) {
    GenerationMovementIllustrations generation = get(movementKey, id);
    generation.discard();
    generations.save(generation);
    log.info("Generation {} discarded for movement {}", id, movementKey);
  }
}
