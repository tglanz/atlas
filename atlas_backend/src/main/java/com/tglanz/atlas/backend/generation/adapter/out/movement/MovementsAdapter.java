package com.tglanz.atlas.backend.generation.adapter.out.movement;

import com.tglanz.atlas.backend.generation.application.port.out.Movements;
import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.ItemType;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementItem;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Reads and updates Movements through the movement module's public use cases. */
@Component
class MovementsAdapter implements Movements {

  private final MovementQueryUseCase queries;
  private final MovementCommandUseCase commands;

  MovementsAdapter(MovementQueryUseCase queries, MovementCommandUseCase commands) {
    this.queries = queries;
    this.commands = commands;
  }

  @Override
  public Optional<Info> find(String movementKey) {
    try {
      Movement movement = queries.get(new MovementKey(movementKey));
      var content = movement.content();
      List<String> cues =
          content.items().stream()
              .filter(item -> item.type() == ItemType.CUE)
              .map(MovementItem::content)
              .toList();
      return Optional.of(new Info(content.name(), content.description(), cues));
    } catch (NotFoundException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  @Override
  public void replaceIllustrations(String movementKey, List<String> urls) {
    commands.replaceIllustrations(
        new MovementKey(movementKey), urls.stream().map(Illustration::new).toList());
  }
}
