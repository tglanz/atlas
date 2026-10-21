package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.domain.Movement;
import java.time.Instant;

record RevisionSummaryResponse(int revision, String name, Instant publishedAt) {

  static RevisionSummaryResponse from(Movement m) {
    return new RevisionSummaryResponse(m.revision(), m.content().name(), m.publishedAt());
  }
}
