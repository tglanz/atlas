package com.tglanz.atlas.backend.generation.adapter.out.movement;

import com.tglanz.atlas.backend.generation.application.port.out.MovementDrafts;
import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Reads and updates Movement drafts through the movement module's public use cases. */
@Component
class MovementDraftsAdapter implements MovementDrafts {

  private final MovementQueryUseCase queries;
  private final MovementCommandUseCase commands;

  MovementDraftsAdapter(MovementQueryUseCase queries, MovementCommandUseCase commands) {
    this.queries = queries;
    this.commands = commands;
  }

  @Override
  public Optional<Draft> findDraft(String movementKey) {
    try {
      Movement draft = queries.getDraft(new MovementKey(movementKey));
      return Optional.of(
          new Draft(draft.revision(), draft.content().name(), draft.content().description()));
    } catch (NotFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public void replaceIllustrations(
      String movementKey, int revision, UUID avatarId, List<String> urls) {
    commands.replaceIllustrations(
        new MovementKey(movementKey),
        revision,
        avatarId,
        urls.stream().map(Illustration::new).toList());
  }

  @Override
  public void addIllustration(String movementKey, UUID avatarId, String url) {
    commands.addIllustration(new MovementKey(movementKey), avatarId, new Illustration(url));
  }
}
