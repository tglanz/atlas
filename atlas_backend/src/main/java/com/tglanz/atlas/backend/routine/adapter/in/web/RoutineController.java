package com.tglanz.atlas.backend.routine.adapter.in.web;

import com.tglanz.atlas.backend.routine.application.port.in.RoutineUseCase;
import com.tglanz.atlas.backend.routine.domain.Routine;
import com.tglanz.atlas.backend.routine.domain.RoutineContent;
import com.tglanz.atlas.backend.routine.domain.Section;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routines")
class RoutineController {

  record SectionDto(
      @NotBlank @Size(max = 200) String name,
      @Size(max = 20000) String description,
      List<@NotBlank String> movements) {}

  record RoutineRequest(
      @NotBlank @Size(max = 200) String name,
      @Size(max = 20000) String description,
      List<@Valid SectionDto> sections) {

    RoutineContent toContent() {
      List<Section> mapped =
          sections == null
              ? List.of()
              : sections.stream()
                  .map(s -> new Section(s.name(), s.description(), s.movements()))
                  .toList();
      return new RoutineContent(name, description, mapped);
    }
  }

  record RoutineResponse(UUID id, String name, String description, List<SectionDto> sections) {
    static RoutineResponse from(Routine r) {
      RoutineContent c = r.content();
      return new RoutineResponse(
          r.id(),
          c.name(),
          c.description(),
          c.sections().stream()
              .map(s -> new SectionDto(s.name(), s.description(), s.movements()))
              .toList());
    }
  }

  record RoutineSummaryResponse(UUID id, String name, String description) {}

  private final RoutineUseCase useCase;

  RoutineController(RoutineUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  List<RoutineSummaryResponse> list(Principal principal) {
    return useCase.list(principal.getName()).stream()
        .map(r -> new RoutineSummaryResponse(r.id(), r.content().name(), r.content().description()))
        .toList();
  }

  @GetMapping("/{id}")
  RoutineResponse get(Principal principal, @PathVariable UUID id) {
    return RoutineResponse.from(useCase.get(principal.getName(), id));
  }

  @PostMapping
  ResponseEntity<RoutineResponse> create(
      Principal principal, @Valid @RequestBody RoutineRequest request) {
    Routine created = useCase.create(principal.getName(), request.toContent());
    return ResponseEntity.created(URI.create("/api/routines/" + created.id()))
        .body(RoutineResponse.from(created));
  }

  @PutMapping("/{id}")
  RoutineResponse update(
      Principal principal, @PathVariable UUID id, @Valid @RequestBody RoutineRequest request) {
    return RoutineResponse.from(useCase.update(principal.getName(), id, request.toContent()));
  }

  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(Principal principal, @PathVariable UUID id) {
    useCase.delete(principal.getName(), id);
    return ResponseEntity.noContent().build();
  }
}
