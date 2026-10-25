package com.tglanz.atlas.backend.graph.adapter.in.web;

import com.tglanz.atlas.backend.graph.application.port.in.GraphUseCase;
import com.tglanz.atlas.backend.graph.domain.Direction;
import com.tglanz.atlas.backend.graph.domain.Edge;
import com.tglanz.atlas.backend.graph.domain.EdgesWithMovements;
import com.tglanz.atlas.backend.graph.domain.Reach;
import com.tglanz.atlas.backend.graph.domain.RelatedMovement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph/movements/{movement}")
class GraphController {

  private static final int MAX_DEPTH = 10;

  record OutgoingEdge(@NotBlank String target, @NotBlank @Size(max = 64) String type) {}

  record SetOutgoingRequest(@NotNull List<@NotNull @Valid OutgoingEdge> edges) {}

  record EdgeResponse(String source, String target, String type) {
    static EdgeResponse from(Edge e) {
      return new EdgeResponse(e.source(), e.target(), e.type());
    }
  }

  record RelatedMovementResponse(
      String key, String name, List<String> tags, String representativeIllustration) {
    static RelatedMovementResponse from(RelatedMovement m) {
      return new RelatedMovementResponse(
          m.key(), m.name(), m.tags(), m.representativeIllustration());
    }
  }

  /** An edge leaving the Movement, with what is shown of the Movement it leads to. */
  record OutgoingEdgeResponse(
      String source, String target, String type, RelatedMovementResponse movement) {}

  record EdgesResponse(List<OutgoingEdgeResponse> outgoing, List<EdgeResponse> incoming) {
    static EdgesResponse from(EdgesWithMovements view) {
      Map<String, RelatedMovementResponse> byKey = new HashMap<>();
      view.targets().forEach(m -> byKey.put(m.key(), RelatedMovementResponse.from(m)));
      return new EdgesResponse(
          view.edges().outgoing().stream()
              .map(
                  e ->
                      new OutgoingEdgeResponse(
                          e.source(), e.target(), e.type(), byKey.get(e.target())))
              .toList(),
          view.edges().incoming().stream().map(EdgeResponse::from).toList());
    }
  }

  record ReachResponse(String movement, int depth, EdgeResponse via) {
    static ReachResponse from(Reach r) {
      return new ReachResponse(r.movement(), r.depth(), EdgeResponse.from(r.via()));
    }
  }

  private final GraphUseCase useCase;

  GraphController(GraphUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping("/edges")
  EdgesResponse edges(@PathVariable String movement) {
    return EdgesResponse.from(useCase.edgesWithMovements(movement));
  }

  @PutMapping("/outgoing")
  ResponseEntity<Void> setOutgoing(
      @PathVariable String movement, @Valid @RequestBody SetOutgoingRequest request) {
    useCase.setOutgoing(
        movement,
        request.edges().stream().map(e -> new Edge(movement, e.target(), e.type())).toList());
    return ResponseEntity.noContent().build();
  }

  /** Movements reachable from this one. Types are comma separated; none means every type. */
  @GetMapping("/traversal")
  List<ReachResponse> traverse(
      @PathVariable String movement,
      @RequestParam(defaultValue = "OUTGOING") Direction direction,
      @RequestParam(required = false) String types,
      @RequestParam(defaultValue = "2") int depth) {
    if (depth < 1 || depth > MAX_DEPTH) {
      throw new IllegalArgumentException("Depth must be between 1 and " + MAX_DEPTH);
    }
    Set<String> typeSet =
        types == null || types.isBlank()
            ? Set.of()
            : Arrays.stream(types.split(","))
                .map(t -> t.strip().toLowerCase())
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toSet());
    return useCase.traverse(movement, direction, typeSet, depth).stream()
        .map(ReachResponse::from)
        .toList();
  }
}
