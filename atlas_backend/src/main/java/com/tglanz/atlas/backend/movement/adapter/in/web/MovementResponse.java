package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import java.util.List;

record MovementResponse(
    String key,
    String name,
    String description,
    List<ItemResponse> items,
    List<StageResponse> stages,
    List<String> tags,
    List<String> illustrations) {

  record ItemResponse(String type, String content) {}

  record StageResponse(String name, String description) {}

  static MovementResponse from(Movement m) {
    MovementContent c = m.content();
    return new MovementResponse(
        m.key().value(),
        c.name(),
        c.description(),
        c.items().stream().map(i -> new ItemResponse(i.type().name(), i.content())).toList(),
        c.stages().stream().map(s -> new StageResponse(s.name(), s.description())).toList(),
        c.tags().stream().sorted().toList(),
        m.illustrations().stream().map(Illustration::imageUrl).toList());
  }
}
