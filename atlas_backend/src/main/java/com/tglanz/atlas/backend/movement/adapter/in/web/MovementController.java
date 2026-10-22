package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movements")
class MovementController {

  private final MovementQueryUseCase queries;
  private final MovementCommandUseCase commands;

  MovementController(MovementQueryUseCase queries, MovementCommandUseCase commands) {
    this.queries = queries;
    this.commands = commands;
  }

  @GetMapping
  List<MovementSummaryResponse> search(
      @RequestParam(required = false) String text, @RequestParam(required = false) String tag) {
    return queries.search(text, tag).stream().map(MovementSummaryResponse::from).toList();
  }

  @GetMapping("/{key}")
  MovementResponse get(@PathVariable String key) {
    return MovementResponse.from(queries.get(new MovementKey(key)));
  }

  @PostMapping
  ResponseEntity<MovementResponse> create(@Valid @RequestBody CreateMovementRequest request) {
    var created = commands.create(new MovementKey(request.key()), request.content().toContent());
    return ResponseEntity.created(URI.create("/api/movements/" + request.key()))
        .body(MovementResponse.from(created));
  }

  @PutMapping("/{key}")
  MovementResponse update(@PathVariable String key, @Valid @RequestBody MovementRequest request) {
    return MovementResponse.from(commands.update(new MovementKey(key), request.toContent()));
  }

  @DeleteMapping("/{key}/illustrations")
  MovementResponse clearIllustrations(@PathVariable String key) {
    return MovementResponse.from(commands.replaceIllustrations(new MovementKey(key), List.of()));
  }
}
