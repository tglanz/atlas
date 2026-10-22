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
    List<String> tags,
    List<String> illustrations) {

  record ItemResponse(String type, String content) {}

  static MovementResponse from(Movement m) {
    MovementContent c = m.content();
    return new MovementResponse(
        m.key().value(),
        c.name(),
        c.description(),
        c.items().stream().map(i -> new ItemResponse(i.type().name(), i.content())).toList(),
        c.tags().stream().sorted().toList(),
        m.illustrations().stream().map(Illustration::imageUrl).toList());
  }
}
