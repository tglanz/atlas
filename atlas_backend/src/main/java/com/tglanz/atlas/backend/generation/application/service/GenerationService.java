package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.in.GenerationUseCase;
import com.tglanz.atlas.backend.generation.application.port.out.AvatarSpecifications;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationRepository;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationScheduler;
import com.tglanz.atlas.backend.generation.application.port.out.MovementDrafts;
import com.tglanz.atlas.backend.generation.application.port.out.ViewpointRepository;
import com.tglanz.atlas.backend.generation.domain.GenerationRequest;
import com.tglanz.atlas.backend.generation.domain.Stage;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GenerationService implements GenerationUseCase {

  private final GenerationRepository requests;
  private final MovementDrafts drafts;
  private final AvatarSpecifications avatars;
  private final ViewpointRepository viewpoints;
  private final GenerationScheduler scheduler;
  private final Clock clock;

  public GenerationService(
      GenerationRepository requests,
      MovementDrafts drafts,
      AvatarSpecifications avatars,
      ViewpointRepository viewpoints,
      GenerationScheduler scheduler,
      Clock clock) {
    this.requests = requests;
    this.drafts = drafts;
    this.avatars = avatars;
    this.viewpoints = viewpoints;
    this.scheduler = scheduler;
    this.clock = clock;
  }

  @Override
  public GenerationRequest request(
      String movementKey, UUID avatarId, UUID viewpointId, List<Stage> stages) {
    var draft =
        drafts
            .findDraft(movementKey)
            .orElseThrow(() -> new NotFoundException("No draft for movement " + movementKey));
    if (avatars.find(avatarId).isEmpty()) {
      throw new IllegalArgumentException("Unknown avatar");
    }
    if (viewpoints.findById(viewpointId).isEmpty()) {
      throw new IllegalArgumentException("Unknown viewpoint");
    }
    GenerationRequest request =
        GenerationRequest.create(
            movementKey, draft.revision(), avatarId, viewpointId, stages, clock.instant());
    requests.save(request);
    scheduler.schedule(request.id());
    return request;
  }

  @Override
  @Transactional(readOnly = true)
  public GenerationRequest get(String movementKey, UUID id) {
    return requests
        .findById(id)
        .filter(r -> r.movementKey().equals(movementKey))
        .orElseThrow(() -> new NotFoundException("Generation request not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public List<GenerationRequest> list(String movementKey) {
    return requests.findByMovementKey(movementKey);
  }

  @Override
  public GenerationRequest retry(String movementKey, UUID id) {
    GenerationRequest request = get(movementKey, id);
    request.retry();
    requests.save(request);
    scheduler.schedule(request.id());
    return request;
  }
}
