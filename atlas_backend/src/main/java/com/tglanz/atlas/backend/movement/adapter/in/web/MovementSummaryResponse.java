package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import java.util.List;

record MovementSummaryResponse(
    String key, int revision, String name, List<String> tags, String representativeIllustration) {

  static MovementSummaryResponse from(Movement m) {
    String illustration =
        m.illustrations().values().stream()
            .flatMap(List::stream)
            .findFirst()
            .map(Illustration::objectKey)
            .orElse(null);
    return new MovementSummaryResponse(
        m.key().value(),
        m.revision(),
        m.content().name(),
        m.content().tags().stream().sorted().toList(),
        illustration);
  }
}
