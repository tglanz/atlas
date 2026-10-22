package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.in.RunGenerationUseCase;
import com.tglanz.atlas.backend.generation.application.port.out.AvatarSpecifications;
import com.tglanz.atlas.backend.generation.application.port.out.ImageConnector;
import com.tglanz.atlas.backend.generation.application.port.out.ImageRequest;
import com.tglanz.atlas.backend.generation.application.port.out.Movements;
import com.tglanz.atlas.backend.generation.application.port.out.ViewpointRepository;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations;
import com.tglanz.atlas.backend.generation.domain.GenerationStatus;
import com.tglanz.atlas.backend.generation.domain.IllustrationStage;
import com.tglanz.atlas.backend.generation.domain.Viewpoint;
import java.time.Clock;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Makes the images of one generation, one stage at a time. The first failure ends the run. */
@Service
public class ImageGenerationWorker implements RunGenerationUseCase {

  private static final Logger log = LoggerFactory.getLogger(ImageGenerationWorker.class);

  private final GenerationLifecycle lifecycle;
  private final ImageConnector connector;
  private final Movements movements;
  private final AvatarSpecifications avatars;
  private final ViewpointRepository viewpoints;
  private final GenerationTemplates templates;
  private final Clock clock;

  public ImageGenerationWorker(
      GenerationLifecycle lifecycle,
      ImageConnector connector,
      Movements movements,
      AvatarSpecifications avatars,
      ViewpointRepository viewpoints,
      GenerationTemplates templates,
      Clock clock) {
    this.lifecycle = lifecycle;
    this.connector = connector;
    this.movements = movements;
    this.avatars = avatars;
    this.viewpoints = viewpoints;
    this.templates = templates;
    this.clock = clock;
  }

  @Override
  public void run(UUID id) {
    GenerationMovementIllustrations generation = lifecycle.find(id).orElse(null);
    if (generation == null || generation.status() != GenerationStatus.PENDING) {
      return;
    }
    generation = lifecycle.update(id, GenerationMovementIllustrations::start);
    log.info(
        "Generation {} started for movement {} with {} stages",
        id,
        generation.movementKey(),
        generation.stages().size());
    var movement = movements.find(generation.movementKey()).orElse(null);
    for (IllustrationStage stage : generation.stages()) {
      int position = stage.position();
      try {
        if (movement == null) {
          throw new IllegalStateException("The movement no longer exists");
        }
        ImageRequest request = compose(movement, stage);
        log.debug("Generation {} stage {} request: {}", id, position, request);
        ImageConnector.Submission submission = connector.submit(request);
        lifecycle.update(
            id,
            g ->
                g.recordSubmission(
                    position,
                    submission.provider(),
                    submission.model(),
                    submission.requestId(),
                    submission.statusUrl(),
                    submission.resultUrl()));
        log.info(
            "Generation {} stage {} submitted as {} request {}",
            id,
            position,
            submission.provider(),
            submission.requestId());
        String imageUrl =
            connector.awaitImageUrl(
                submission, text -> lifecycle.update(id, g -> g.recordProgress(position, text)));
        var done = lifecycle.update(id, g -> g.recordDone(position, imageUrl, clock.instant()));
        log.info("Generation {} stage {} done: {}", id, position, imageUrl);
        if (done.status() == GenerationStatus.READY_FOR_REVIEW) {
          log.info("Generation {} is ready for review", id);
        }
      } catch (RuntimeException e) {
        log.warn("Generation {} failed at stage {}: {}", id, position, describe(e), e);
        lifecycle.update(id, g -> g.recordFailed(position, describe(e), clock.instant()));
        return;
      }
    }
  }

  @Override
  public void failInterrupted() {
    for (GenerationMovementIllustrations g : lifecycle.findInProgress()) {
      log.warn("Generation {} was interrupted by a restart; failing it", g.id());
      lifecycle.update(g.id(), x -> x.failInterrupted("Interrupted by a restart", clock.instant()));
    }
  }

  private ImageRequest compose(Movements.Info movement, IllustrationStage stage) {
    String avatar =
        avatars
            .find(stage.avatarId())
            .orElseThrow(() -> new IllegalStateException("The avatar no longer exists"));
    Viewpoint viewpoint =
        viewpoints
            .findById(stage.viewpointId())
            .orElseThrow(() -> new IllegalStateException("The viewpoint no longer exists"));
    return templates.render(
        new GenerationTemplates.Model(
            movement.name(),
            movement.description(),
            movement.cues(),
            avatar,
            viewpoint.specification(),
            stage.name(),
            stage.description()));
  }

  private static String describe(RuntimeException e) {
    String message = e.getMessage();
    return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
  }
}
