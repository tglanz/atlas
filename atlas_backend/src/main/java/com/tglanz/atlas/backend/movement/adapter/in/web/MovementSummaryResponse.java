package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.domain.Movement;
import java.util.List;

record MovementSummaryResponse(
    String key, String name, List<String> tags, String representativeIllustration) {

  static MovementSummaryResponse from(Movement m) {
    String first = m.illustrations().isEmpty() ? null : m.illustrations().get(0).imageUrl();
    return new MovementSummaryResponse(
        m.key().value(), m.content().name(), m.content().tags().stream().sorted().toList(), first);
  }
}
