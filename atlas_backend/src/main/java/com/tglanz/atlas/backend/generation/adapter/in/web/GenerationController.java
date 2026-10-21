package com.tglanz.atlas.backend.generation.adapter.in.web;

import com.tglanz.atlas.backend.generation.application.port.in.GenerationUseCase;
import com.tglanz.atlas.backend.generation.domain.GenerationRequest;
import com.tglanz.atlas.backend.generation.domain.Stage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movements/{key}/draft/generations")
class GenerationController {

  record StageDto(
      @Size(max = 2000) String description,
      @Size(max = 500) @Pattern(regexp = "|https?://.+") String referenceUrl) {}

  record GenerateRequest(
      @NotNull UUID avatarId,
      @NotNull UUID viewpointId,
      @NotEmpty @Size(max = 20) List<@NotNull @Valid StageDto> stages) {}

  record GenerationResponse(
      UUID id,
      String movementKey,
      int draftRevision,
      UUID avatarId,
      UUID viewpointId,
      List<StageDto> stages,
      String status,
      String failureReason,
      List<String> results,
      Instant createdAt,
      Instant completedAt) {
    static GenerationResponse from(GenerationRequest r) {
      return new GenerationResponse(
          r.id(),
          r.movementKey(),
          r.draftRevision(),
          r.avatarId(),
          r.viewpointId(),
          r.stages().stream().map(s -> new StageDto(s.description(), s.referenceUrl())).toList(),
          r.status().name(),
          r.failureReason(),
          r.resultUrls(),
          r.createdAt(),
          r.completedAt());
    }
  }

  private final GenerationUseCase useCase;

  GenerationController(GenerationUseCase useCase) {
    this.useCase = useCase;
  }

  @PostMapping
  ResponseEntity<GenerationResponse> request(
      @PathVariable String key, @Valid @RequestBody GenerateRequest body) {
    List<Stage> stages =
        body.stages().stream().map(s -> new Stage(s.description(), s.referenceUrl())).toList();
    GenerationRequest created = useCase.request(key, body.avatarId(), body.viewpointId(), stages);
    return ResponseEntity.accepted()
        .location(URI.create("/api/movements/" + key + "/draft/generations/" + created.id()))
        .body(GenerationResponse.from(created));
  }

  @GetMapping
  List<GenerationResponse> list(@PathVariable String key) {
    return useCase.list(key).stream().map(GenerationResponse::from).toList();
  }

  @GetMapping("/{id}")
  GenerationResponse get(@PathVariable String key, @PathVariable UUID id) {
    return GenerationResponse.from(useCase.get(key, id));
  }

  @PostMapping("/{id}/retry")
  ResponseEntity<GenerationResponse> retry(@PathVariable String key, @PathVariable UUID id) {
    return ResponseEntity.accepted().body(GenerationResponse.from(useCase.retry(key, id)));
  }
}
