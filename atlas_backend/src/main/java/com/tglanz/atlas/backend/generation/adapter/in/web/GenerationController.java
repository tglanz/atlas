package com.tglanz.atlas.backend.generation.adapter.in.web;

import com.tglanz.atlas.backend.generation.application.port.in.GenerationUseCase;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations.StageDefinition;
import com.tglanz.atlas.backend.generation.domain.IllustrationCandidate;
import com.tglanz.atlas.backend.generation.domain.IllustrationStage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/movements/{key}/generations")
class GenerationController {

  record IllustrationStageInput(
      @NotNull UUID avatarId,
      @NotNull UUID viewpointId,
      @NotBlank @Size(max = 4000) String instructions) {}

  record GenerationMovementIllustrationsRequest(
      @NotEmpty @Size(max = 20) List<@NotNull @Valid IllustrationStageInput> stages) {}

  record IllustrationCandidateResponse(
      String state, String imageUrl, String failureReason, String progress) {
    static IllustrationCandidateResponse from(IllustrationCandidate c) {
      return new IllustrationCandidateResponse(
          c.state().name(), c.imageUrl(), c.failureReason(), c.progress());
    }
  }

  record IllustrationStageResponse(
      int position,
      UUID avatarId,
      UUID viewpointId,
      String instructions,
      IllustrationCandidateResponse candidate) {
    static IllustrationStageResponse from(IllustrationStage s) {
      return new IllustrationStageResponse(
          s.position(),
          s.avatarId(),
          s.viewpointId(),
          s.instructions(),
          IllustrationCandidateResponse.from(s.candidate()));
    }
  }

  record GenerationMovementIllustrationsResponse(
      UUID id,
      String movementKey,
      String status,
      String failureReason,
      Instant createdAt,
      Instant finishedAt,
      List<IllustrationStageResponse> stages) {
    static GenerationMovementIllustrationsResponse from(GenerationMovementIllustrations g) {
      return new GenerationMovementIllustrationsResponse(
          g.id(),
          g.movementKey(),
          g.status().name(),
          g.failureReason(),
          g.createdAt(),
          g.finishedAt(),
          g.stages().stream().map(IllustrationStageResponse::from).toList());
    }
  }

  private final GenerationUseCase useCase;

  GenerationController(GenerationUseCase useCase) {
    this.useCase = useCase;
  }

  @PostMapping
  ResponseEntity<GenerationMovementIllustrationsResponse> request(
      @PathVariable String key, @Valid @RequestBody GenerationMovementIllustrationsRequest body) {
    List<StageDefinition> stages =
        body.stages().stream()
            .map(s -> new StageDefinition(s.avatarId(), s.viewpointId(), s.instructions()))
            .toList();
    GenerationMovementIllustrations created = useCase.request(key, stages);
    return ResponseEntity.accepted()
        .location(URI.create("/api/movements/" + key + "/generations/" + created.id()))
        .body(GenerationMovementIllustrationsResponse.from(created));
  }

  @GetMapping
  List<GenerationMovementIllustrationsResponse> list(@PathVariable String key) {
    return useCase.list(key).stream().map(GenerationMovementIllustrationsResponse::from).toList();
  }

  @GetMapping("/{id}")
  GenerationMovementIllustrationsResponse get(@PathVariable String key, @PathVariable UUID id) {
    return GenerationMovementIllustrationsResponse.from(useCase.get(key, id));
  }

  @PostMapping("/{id}/apply")
  GenerationMovementIllustrationsResponse apply(@PathVariable String key, @PathVariable UUID id) {
    return GenerationMovementIllustrationsResponse.from(useCase.apply(key, id));
  }

  @PostMapping("/{id}/discard")
  ResponseEntity<Void> discard(@PathVariable String key, @PathVariable UUID id) {
    useCase.discard(key, id);
    return ResponseEntity.noContent().build();
  }
}
