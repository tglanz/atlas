package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
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
      @RequestParam(required = false) String text,
      @RequestParam(required = false) String tag,
      Principal principal) {
    return queries.search(text, tag, viewerId(principal)).stream()
        .map(MovementSummaryResponse::from)
        .toList();
  }

  @GetMapping("/{key}")
  MovementResponse current(@PathVariable String key, Principal principal) {
    return MovementResponse.from(queries.getCurrent(new MovementKey(key), viewerId(principal)));
  }

  @GetMapping("/{key}/revisions")
  List<RevisionSummaryResponse> revisions(@PathVariable String key) {
    return queries.listPublishedRevisions(new MovementKey(key)).stream()
        .map(RevisionSummaryResponse::from)
        .toList();
  }

  @GetMapping("/{key}/revisions/{revision}")
  MovementResponse revision(
      @PathVariable String key, @PathVariable int revision, Principal principal) {
    return MovementResponse.from(
        queries.getPublishedRevision(new MovementKey(key), revision, viewerId(principal)));
  }

  @GetMapping("/{key}/draft")
  MovementEditorResponse draft(@PathVariable String key) {
    return MovementEditorResponse.from(queries.getDraft(new MovementKey(key)));
  }

  @PostMapping
  ResponseEntity<MovementEditorResponse> create(@Valid @RequestBody CreateMovementRequest request) {
    Movement draft =
        commands.createDraft(new MovementKey(request.key()), request.content().toContent());
    return ResponseEntity.created(URI.create("/api/movements/" + request.key() + "/draft"))
        .body(MovementEditorResponse.from(draft));
  }

  @PutMapping("/{key}/draft")
  MovementEditorResponse updateDraft(
      @PathVariable String key, @Valid @RequestBody MovementRequest request) {
    return MovementEditorResponse.from(
        commands.updateDraft(new MovementKey(key), request.toContent()));
  }

  @PostMapping("/{key}/revisions")
  ResponseEntity<MovementEditorResponse> createRevision(@PathVariable String key) {
    Movement draft = commands.createRevision(new MovementKey(key));
    return ResponseEntity.created(URI.create("/api/movements/" + key + "/draft"))
        .body(MovementEditorResponse.from(draft));
  }

  @PostMapping("/{key}/draft/publish")
  MovementEditorResponse publish(@PathVariable String key) {
    return MovementEditorResponse.from(commands.publish(new MovementKey(key)));
  }

  @DeleteMapping("/{key}/draft/avatars/{avatarId}/illustrations")
  MovementEditorResponse clearIllustrations(@PathVariable String key, @PathVariable UUID avatarId) {
    return MovementEditorResponse.from(commands.clearIllustrations(new MovementKey(key), avatarId));
  }

  private static String viewerId(Principal principal) {
    return principal == null ? null : principal.getName();
  }
}
