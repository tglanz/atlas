package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.in.GenerationDraft;
import com.tglanz.atlas.backend.generation.application.port.in.GenerationUseCase;
import com.tglanz.atlas.backend.generation.application.port.out.AvatarSpecifications;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationRepository;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationScheduler;
import com.tglanz.atlas.backend.generation.application.port.out.Movements;
import com.tglanz.atlas.backend.generation.application.port.out.ViewpointRepository;
import com.tglanz.atlas.backend.generation.domain.GenerationInput;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations.StageDefinition;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
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
  public GenerationMovementIllustrations request(
      String movementKey, UUID avatarId, UUID viewpointId) {
    return request(movementKey, avatarId, viewpointId, null, null);
  }

  @Override
  public GenerationMovementIllustrations request(
      String movementKey,
      UUID avatarId,
      UUID viewpointId,
      GenerationDraft draft,
      Integer stagePosition) {
    Movements.Info movement =
        movements
            .find(movementKey)
            .orElseThrow(() -> new NotFoundException("No movement " + movementKey));
    if (movement.stages().isEmpty()) {
      throw new IllegalArgumentException("The movement has no stages to generate");
    }
    String avatar =
        avatars.find(avatarId).orElseThrow(() -> new IllegalArgumentException("Unknown avatar"));
    var viewpoint =
        viewpoints
            .findById(viewpointId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown viewpoint"));
    if (draft == null) {
      draft =
          new GenerationDraft(
              movement.name(),
              movement.description(),
              movement.cues(),
              movement.stages().stream()
                  .map(s -> new GenerationDraft.Stage(s.name(), s.description()))
                  .toList(),
              "");
    }
    if (draft.stages().size() != movement.stages().size()) {
      throw new IllegalArgumentException(
          "The stages have changed. Reload the movement before generating.");
    }
    if (stagePosition != null && (stagePosition < 0 || stagePosition >= draft.stages().size())) {
      throw new IllegalArgumentException("Choose an existing stage");
    }
    var input =
        new GenerationInput(
            draft.name(),
            draft.description(),
            draft.cues(),
            draft.guidelines(),
            avatar,
            viewpoint.specification());
    if (generations.existsInProgress(movementKey)) {
      throw new ConflictException("This movement already has a generation in progress");
    }
    List<StageDefinition> stages =
        (stagePosition == null ? draft.stages() : List.of(draft.stages().get(stagePosition)))
            .stream()
                .map(s -> new StageDefinition(avatarId, viewpointId, s.name(), s.description()))
                .toList();
    var generation =
        GenerationMovementIllustrations.create(
            movementKey,
            stages,
            clock.instant(),
            input,
            fingerprint(movement.stages()),
            stagePosition);
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
    var movement =
        movements
            .find(movementKey)
            .orElseThrow(() -> new NotFoundException("No movement " + movementKey));
    if (generation.stageFingerprint() != null
        && !generation.stageFingerprint().equals(fingerprint(movement.stages()))) {
      throw new ConflictException(
          "The movement stages changed since this generation. Generate again before applying.");
    }
    var urls = generation.approve();
    if (generation.targetStagePosition() == null) {
      movements.replaceIllustrations(movementKey, urls);
    } else {
      movements.replaceStageIllustration(
          movementKey, generation.targetStagePosition(), urls.getFirst());
    }
    generations.save(generation);
    log.info("Generation {} applied to movement {}", id, movementKey);
    return generation;
  }

  private static String fingerprint(List<Movements.Stage> stages) {
    try {
      var digest = MessageDigest.getInstance("SHA-256");
      for (var stage : stages) {
        String text =
            stage.name().length()
                + ":"
                + stage.name()
                + stage.description().length()
                + ":"
                + stage.description();
        digest.update(text.getBytes(StandardCharsets.UTF_8));
      }
      return HexFormat.of().formatHex(digest.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void discard(String movementKey, UUID id) {
    GenerationMovementIllustrations generation = get(movementKey, id);
    generation.discard();
    generations.save(generation);
    log.info("Generation {} discarded for movement {}", id, movementKey);
  }
}
