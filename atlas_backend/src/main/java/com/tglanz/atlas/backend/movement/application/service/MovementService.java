package com.tglanz.atlas.backend.movement.application.service;

import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.application.port.out.MovementRepository;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.RevisionStatus;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MovementService implements MovementQueryUseCase, MovementCommandUseCase {

  private final MovementRepository repository;
  private final Clock clock;

  public MovementService(MovementRepository repository, Clock clock) {
    this.repository = repository;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Movement> search(String text, String tag) {
    return repository.searchCurrentPublished(blankToEmpty(text), blankToEmpty(tag).toLowerCase());
  }

  @Override
  @Transactional(readOnly = true)
  public Movement getCurrent(MovementKey key) {
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
  public Movement getPublishedRevision(MovementKey key, int revision) {
    return repository
        .find(key, revision)
        .filter(m -> m.status() == RevisionStatus.PUBLISHED)
        .orElseThrow(
            () -> new NotFoundException("No published revision " + revision + " of " + key));
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
    Movement latest = getCurrent(key);
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

  private static String blankToEmpty(String value) {
    return value == null ? "" : value.strip();
  }
}
