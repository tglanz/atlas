package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.domain.MovementSummary;
import java.util.List;

record MovementSummaryResponse(
    String key, String name, List<String> tags, String representativeIllustration) {

  static MovementSummaryResponse from(MovementSummary s) {
    return new MovementSummaryResponse(
        s.key().value(), s.name(), s.tags(), s.representativeIllustration());
  }
}
