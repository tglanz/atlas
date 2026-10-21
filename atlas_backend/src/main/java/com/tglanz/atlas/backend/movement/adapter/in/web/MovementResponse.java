package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementView;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A revision for viewers: Illustrations of the single Avatar chosen for the viewer. */
record MovementResponse(
    String key,
    int revision,
    String status,
    Instant publishedAt,
    String name,
    String description,
    List<String> cues,
    List<String> tags,
    List<MovementEditorResponse.LinkResponse> links,
    UUID avatarId,
    List<String> illustrations) {

  static MovementResponse from(MovementView view) {
    Movement m = view.movement();
    MovementContent c = m.content();
    return new MovementResponse(
        m.key().value(),
        m.revision(),
        m.status().name(),
        m.publishedAt(),
        c.name(),
        c.description(),
        c.cues(),
        c.tags().stream().sorted().toList(),
        c.links().stream()
            .map(l -> new MovementEditorResponse.LinkResponse(l.target().value(), l.relationType()))
            .toList(),
        view.avatarId(),
        view.illustrations().stream().map(i -> i.url()).toList());
  }
}
