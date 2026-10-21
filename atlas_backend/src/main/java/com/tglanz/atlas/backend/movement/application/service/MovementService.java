package com.tglanz.atlas.backend.movement.application.service;

import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.application.port.out.AvatarSelector;
import com.tglanz.atlas.backend.movement.application.port.out.KnownAvatars;
import com.tglanz.atlas.backend.movement.application.port.out.MovementRepository;
import com.tglanz.atlas.backend.movement.domain.AvatarChoice;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementView;
import com.tglanz.atlas.backend.movement.domain.RevisionStatus;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MovementService implements MovementQueryUseCase, MovementCommandUseCase {

  private final MovementRepository repository;
  private final AvatarSelector avatars;
  private final KnownAvatars knownAvatars;
  private final Clock clock;

  public MovementService(
      MovementRepository repository,
      AvatarSelector avatars,
      KnownAvatars knownAvatars,
      Clock clock) {
    this.repository = repository;
    this.avatars = avatars;
    this.knownAvatars = knownAvatars;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public List<MovementView> search(String text, String tag, String viewerId) {
    AvatarChoice choice = avatars.choiceFor(viewerId);
    return repository
        .searchCurrentPublished(blankToEmpty(text), blankToEmpty(tag).toLowerCase())
        .stream()
        .map(m -> MovementView.of(m, choice))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public MovementView getCurrent(MovementKey key, String viewerId) {
    return MovementView.of(latestPublished(key), avatars.choiceFor(viewerId));
  }

  private Movement latestPublished(MovementKey key) {
    return repository
        .findLatestPublished(key)
        .orElseThrow(() -> new NotFoundException("No published movement " + key));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Movement> listPublishedRevisions(MovementKey key) {
    List<Movement> published =
        repository.findAllRevisions(key).stream()
            .filter(m -> m.status() == RevisionStatus.PUBLISHED)
            .toList();
    if (published.isEmpty()) {
      throw new NotFoundException("No published movement " + key);
    }
    return published;
  }

  @Override
  @Transactional(readOnly = true)
  public MovementView getPublishedRevision(MovementKey key, int revision, String viewerId) {
    Movement movement =
        repository
            .find(key, revision)
            .filter(m -> m.status() == RevisionStatus.PUBLISHED)
            .orElseThrow(
                () -> new NotFoundException("No published revision " + revision + " of " + key));
    return MovementView.of(movement, avatars.choiceFor(viewerId));
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isPublished(MovementKey key, int revision) {
    return repository
        .find(key, revision)
        .filter(m -> m.status() == RevisionStatus.PUBLISHED)
        .isPresent();
  }

  @Override
  @Transactional(readOnly = true)
  public Movement getDraft(MovementKey key) {
    return repository
        .findDraft(key)
        .orElseThrow(() -> new NotFoundException("No draft for movement " + key));
  }

  @Override
  public Movement createDraft(MovementKey key, MovementContent content) {
    if (repository.existsKey(key)) {
      throw new ConflictException("Movement " + key + " already exists");
    }
    Movement draft = Movement.newDraft(key, content);
    repository.save(draft);
    return draft;
  }

  @Override
  public Movement updateDraft(MovementKey key, MovementContent content) {
    Movement draft = getDraft(key);
    draft.edit(content);
    repository.save(draft);
    return draft;
  }

  @Override
  public Movement createRevision(MovementKey key) {
    Movement latest = latestPublished(key);
    if (repository.findDraft(key).isPresent()) {
      throw new ConflictException("Movement " + key + " already has a draft");
    }
    Movement draft = latest.revise();
    repository.save(draft);
    return draft;
  }

  @Override
  public Movement publish(MovementKey key) {
    Movement draft = getDraft(key);
    draft.publish(clock.instant());
    repository.save(draft);
    return draft;
  }

  @Override
  public Movement addIllustration(MovementKey key, UUID avatarId, Illustration illustration) {
    requireAvatar(avatarId);
    Movement draft = getDraft(key);
    draft.addIllustration(avatarId, illustration);
    repository.save(draft);
    return draft;
  }

  @Override
  public Movement replaceIllustrations(
      MovementKey key, int expectedRevision, UUID avatarId, List<Illustration> illustrations) {
    requireAvatar(avatarId);
    Movement draft = getDraft(key);
    if (draft.revision() != expectedRevision) {
      throw new ConflictException(
          "Draft revision " + expectedRevision + " of " + key + " is no longer open");
    }
    draft.replaceIllustrations(avatarId, illustrations);
    repository.save(draft);
    return draft;
  }

  @Override
  public Movement clearIllustrations(MovementKey key, UUID avatarId) {
    Movement draft = getDraft(key);
    draft.replaceIllustrations(avatarId, List.of());
    repository.save(draft);
    return draft;
  }

  private void requireAvatar(UUID avatarId) {
    if (avatarId == null || !knownAvatars.exists(avatarId)) {
      throw new IllegalArgumentException("Unknown avatar");
    }
  }

  private static String blankToEmpty(String value) {
    return value == null ? "" : value.strip();
  }
}
