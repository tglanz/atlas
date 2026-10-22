package com.tglanz.atlas.backend.graph.adapter.in.web;

import com.tglanz.atlas.backend.graph.application.port.in.GraphUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph/edge-types")
class EdgeTypeController {

  record EdgeTypeResponse(String name, String label, boolean bidirectional) {}

  private final GraphUseCase useCase;

  EdgeTypeController(GraphUseCase useCase) {
    this.useCase = useCase;
  }

  /** The relationship types to offer. Edges may carry other types; this is not a restriction. */
  @GetMapping
  List<EdgeTypeResponse> list() {
    return useCase.edgeTypes().stream()
        .map(t -> new EdgeTypeResponse(t.name(), t.label(), t.bidirectional()))
        .toList();
  }
}
