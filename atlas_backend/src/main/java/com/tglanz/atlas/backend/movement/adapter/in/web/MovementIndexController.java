package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The lists the Movement Index page searches with: every name, and every tag. */
@RestController
@RequestMapping("/api/movement-index")
class MovementIndexController {

  record NameResponse(String key, String name) {}

  record TagResponse(String tag, long count) {}

  private final MovementQueryUseCase queries;

  MovementIndexController(MovementQueryUseCase queries) {
    this.queries = queries;
  }

  @GetMapping("/names")
  List<NameResponse> names() {
    return queries.names().stream().map(n -> new NameResponse(n.key().value(), n.name())).toList();
  }

  @GetMapping("/tags")
  List<TagResponse> tags() {
    return queries.tags().stream().map(t -> new TagResponse(t.tag(), t.count())).toList();
  }
}
