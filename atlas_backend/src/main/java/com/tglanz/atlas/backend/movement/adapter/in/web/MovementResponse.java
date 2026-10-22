package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import java.util.List;

record MovementResponse(
    String key,
    String name,
    String description,
    List<String> cues,
    List<String> tags,
    List<LinkResponse> links,
    List<String> illustrations) {

  record LinkResponse(String target, String relationType) {}

  static MovementResponse from(Movement m) {
    MovementContent c = m.content();
    return new MovementResponse(
        m.key().value(),
        c.name(),
        c.description(),
        c.cues(),
        c.tags().stream().sorted().toList(),
        c.links().stream()
            .map(l -> new LinkResponse(l.target().value(), l.relationType()))
            .toList(),
        m.illustrations().stream().map(Illustration::url).toList());
  }
}
