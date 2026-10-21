package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.domain.MovementView;
import java.util.List;

record MovementSummaryResponse(
    String key, int revision, String name, List<String> tags, String representativeIllustration) {

  static MovementSummaryResponse from(MovementView view) {
    var m = view.movement();
    String first = view.illustrations().isEmpty() ? null : view.illustrations().get(0).url();
    return new MovementSummaryResponse(
        m.key().value(),
        m.revision(),
        m.content().name(),
        m.content().tags().stream().sorted().toList(),
        first);
  }
}
