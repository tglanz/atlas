package com.tglanz.atlas.backend.generation.adapter.in.web;

import com.tglanz.atlas.backend.generation.application.port.in.GenerationUseCase;
import com.tglanz.atlas.backend.generation.domain.AvatarImageSpec;
import com.tglanz.atlas.backend.generation.domain.GenerationRequest;
import com.tglanz.atlas.backend.generation.domain.IllustrationsGenerationSpec;
import com.tglanz.atlas.backend.generation.domain.StageSpec;
import com.tglanz.atlas.backend.generation.domain.StaticImageSpec;
import jakarta.validation.Valid;
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

  record AvatarStage(
      @NotNull UUID avatarId,
      @NotNull UUID viewpointId,
      @Size(max = 4000) String instructions,
      @Size(max = 500) String referenceImageUrl) {}

  record StaticStage(@NotNull @Size(max = 500) String imageUrl) {}

  /** Exactly one of the two is set. */
  record StageBody(@Valid AvatarStage avatar, @Valid StaticStage staticImage) {
    StageSpec toSpec() {
      if ((avatar == null) == (staticImage == null)) {
        throw new IllegalArgumentException(
            "A stage is either avatar-based or a static image, not both or neither");
      }
      return avatar != null
          ? new AvatarImageSpec(
              avatar.avatarId(),
              avatar.viewpointId(),
              avatar.instructions(),
              avatar.referenceImageUrl())
          : new StaticImageSpec(staticImage.imageUrl());
    }

    static StageBody from(StageSpec spec) {
      return switch (spec) {
        case AvatarImageSpec s ->
            new StageBody(
                new AvatarStage(
                    s.avatarId(), s.viewpointId(), s.instructions(), s.referenceImageUrl()),
                null);
        case StaticImageSpec s -> new StageBody(null, new StaticStage(s.imageUrl()));
      };
    }
  }

  record GenerateRequest(@NotEmpty @Size(max = 20) List<@NotNull @Valid StageBody> stages) {}

  record GenerationResponse(
      UUID id,
      String movementKey,
      List<StageBody> stages,
      String status,
      String failureReason,
      List<String> results,
      Instant createdAt,
      Instant completedAt) {
    static GenerationResponse from(GenerationRequest r) {
      return new GenerationResponse(
          r.id(),
          r.movementKey(),
          r.spec().stages().stream().map(StageBody::from).toList(),
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
    var spec =
        new IllustrationsGenerationSpec(body.stages().stream().map(StageBody::toSpec).toList());
    GenerationRequest created = useCase.request(key, spec);
    return ResponseEntity.accepted()
        .location(URI.create("/api/movements/" + key + "/generations/" + created.id()))
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
