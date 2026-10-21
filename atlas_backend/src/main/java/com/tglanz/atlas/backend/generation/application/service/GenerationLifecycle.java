package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.out.AvatarSpecifications;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationInput;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationRepository;
import com.tglanz.atlas.backend.generation.application.port.out.MovementDrafts;
import com.tglanz.atlas.backend.generation.application.port.out.ViewpointRepository;
import com.tglanz.atlas.backend.generation.domain.GenerationRequest;
import com.tglanz.atlas.backend.generation.domain.GenerationStatus;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The short transactions around a generation run. The slow generator call happens between them, so
 * no database transaction is held open while waiting on the third party.
 */
@Service
public class GenerationLifecycle {

  private final GenerationRepository requests;
  private final MovementDrafts drafts;
  private final AvatarSpecifications avatars;
  private final ViewpointRepository viewpoints;
  private final Clock clock;

  public GenerationLifecycle(
      GenerationRepository requests,
      MovementDrafts drafts,
      AvatarSpecifications avatars,
      ViewpointRepository viewpoints,
      Clock clock) {
    this.requests = requests;
    this.drafts = drafts;
    this.avatars = avatars;
    this.viewpoints = viewpoints;
    this.clock = clock;
  }

  /** Marks the request running and returns the generator input, or empty if it is not pending. */
  @Transactional
  public Optional<GenerationInput> begin(UUID id) {
    GenerationRequest request = requests.findById(id).orElse(null);
    if (request == null || request.status() != GenerationStatus.PENDING) {
      return Optional.empty();
    }
    var draft = drafts.findDraft(request.movementKey()).orElse(null);
    if (draft == null || draft.revision() != request.draftRevision()) {
      request.fail("The draft this request was made for is no longer open", clock.instant());
      requests.save(request);
      return Optional.empty();
    }
    String avatar = avatars.find(request.avatarId()).orElse(null);
    var viewpoint = viewpoints.findById(request.viewpointId()).orElse(null);
    if (avatar == null || viewpoint == null) {
      request.fail("The avatar or viewpoint no longer exists", clock.instant());
      requests.save(request);
      return Optional.empty();
    }
    request.start();
    requests.save(request);
    return Optional.of(
        new GenerationInput(
            draft.name(),
            draft.description(),
            avatar,
            viewpoint.specification(),
            request.stages()));
  }

  /** Attaches the generated Illustrations to the draft, unless it has moved on. */
  @Transactional
  public void complete(UUID id, List<String> urls) {
    GenerationRequest request = requests.findById(id).orElseThrow();
    var draft = drafts.findDraft(request.movementKey()).orElse(null);
    if (draft == null || draft.revision() != request.draftRevision()) {
      request.fail("The draft this request was made for is no longer open", clock.instant());
    } else {
      drafts.replaceIllustrations(
          request.movementKey(), draft.revision(), request.avatarId(), urls);
      request.succeed(urls, clock.instant());
    }
    requests.save(request);
  }

  @Transactional
  public void fail(UUID id, String reason) {
    requests
        .findById(id)
        .ifPresent(
            request -> {
              request.fail(reason, clock.instant());
              requests.save(request);
            });
  }
}
