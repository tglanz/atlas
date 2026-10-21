package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Full revision for maintainers: Illustrations of every Avatar. */
record MovementEditorResponse(
    String key,
    int revision,
    String status,
    Instant publishedAt,
    String name,
    String description,
    List<String> cues,
    List<String> tags,
    List<LinkResponse> links,
    Map<UUID, List<IllustrationResponse>> illustrations) {

  record LinkResponse(String target, String relationType) {}

  record IllustrationResponse(String url) {}

  static MovementEditorResponse from(Movement m) {
    MovementContent c = m.content();
    return new MovementEditorResponse(
        m.key().value(),
        m.revision(),
        m.status().name(),
        m.publishedAt(),
        c.name(),
        c.description(),
        c.cues(),
        c.tags().stream().sorted().toList(),
        c.links().stream()
            .map(l -> new LinkResponse(l.target().value(), l.relationType()))
            .toList(),
        m.illustrations().entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().stream().map(MovementEditorResponse::illustration).toList(),
                    (a, b) -> a,
                    java.util.LinkedHashMap::new)));
  }

  private static IllustrationResponse illustration(Illustration i) {
    return new IllustrationResponse(i.url());
  }
}
