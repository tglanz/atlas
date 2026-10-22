package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.out.AvatarSpecifications;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationRepository;
import com.tglanz.atlas.backend.generation.application.port.out.ImageRequest;
import com.tglanz.atlas.backend.generation.application.port.out.Movements;
import com.tglanz.atlas.backend.generation.application.port.out.ViewpointRepository;
import com.tglanz.atlas.backend.generation.domain.AvatarImageSpec;
import com.tglanz.atlas.backend.generation.domain.GenerationRequest;
import com.tglanz.atlas.backend.generation.domain.GenerationStatus;
import com.tglanz.atlas.backend.generation.domain.StageSpec;
import com.tglanz.atlas.backend.generation.domain.StaticImageSpec;
import com.tglanz.atlas.backend.generation.domain.Viewpoint;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The short transactions around a generation run. Slow generator calls happen between them, so no
 * database transaction is held open while waiting on the third party.
 */
@Service
public class GenerationLifecycle {

  private static final String MOVEMENT_GONE = "The movement no longer exists";

  private final GenerationRepository requests;
  private final Movements movements;
  private final AvatarSpecifications avatars;
  private final ViewpointRepository viewpoints;
  private final Clock clock;

  public GenerationLifecycle(
      GenerationRepository requests,
      Movements movements,
      AvatarSpecifications avatars,
      ViewpointRepository viewpoints,
      Clock clock) {
    this.requests = requests;
    this.movements = movements;
    this.avatars = avatars;
    this.viewpoints = viewpoints;
    this.clock = clock;
  }

  /** Marks the request running and returns what to do, or empty if it is not runnable. */
  @Transactional
  public Optional<GenerationPlan> begin(UUID id) {
    GenerationRequest request = requests.findById(id).orElse(null);
    if (request == null || request.status() != GenerationStatus.PENDING) {
      return Optional.empty();
    }
    var movement = movements.find(request.movementKey()).orElse(null);
    if (movement == null) {
      return fail(request, MOVEMENT_GONE);
    }
    List<GenerationPlan.Step> steps = new ArrayList<>();
    for (StageSpec stage : request.spec().stages()) {
      switch (stage) {
        case StaticImageSpec s -> steps.add(new GenerationPlan.Existing(s.imageUrl()));
        case AvatarImageSpec s -> {
          String avatar = avatars.find(s.avatarId()).orElse(null);
          Viewpoint viewpoint = viewpoints.findById(s.viewpointId()).orElse(null);
          if (avatar == null || viewpoint == null) {
            return fail(request, "An avatar or viewpoint no longer exists");
          }
          steps.add(
              new GenerationPlan.Generate(
                  new ImageRequest(
                      movement.name(),
                      movement.description(),
                      avatar,
                      viewpoint.specification(),
                      s.instructions(),
                      s.referenceImageUrl())));
        }
      }
    }
    request.start();
    requests.save(request);
    return Optional.of(new GenerationPlan(steps));
  }

  /** Replaces the Movement's Illustrations with the produced ones. */
  @Transactional
  public void complete(UUID id, List<String> urls) {
    GenerationRequest request = requests.findById(id).orElseThrow();
    if (movements.find(request.movementKey()).isEmpty()) {
      request.fail(MOVEMENT_GONE, clock.instant());
    } else {
      movements.replaceIllustrations(request.movementKey(), urls);
      request.succeed(urls, clock.instant());
    }
    requests.save(request);
  }

  @Transactional
  public void fail(UUID id, String reason) {
    requests.findById(id).ifPresent(request -> fail(request, reason));
  }

  private Optional<GenerationPlan> fail(GenerationRequest request, String reason) {
    request.fail(reason, clock.instant());
    requests.save(request);
    return Optional.empty();
  }
}
