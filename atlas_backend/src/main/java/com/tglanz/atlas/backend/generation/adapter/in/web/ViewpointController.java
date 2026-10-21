package com.tglanz.atlas.backend.generation.adapter.in.web;

import com.tglanz.atlas.backend.generation.application.port.in.ViewpointUseCase;
import com.tglanz.atlas.backend.generation.domain.Viewpoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
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
@RequestMapping("/api/viewpoints")
class ViewpointController {

  record Request(@NotBlank @Size(max = 100) String name, @NotBlank String specification) {}

  record Response(UUID id, String name, String specification) {
    static Response from(Viewpoint viewpoint) {
      return new Response(viewpoint.id(), viewpoint.name(), viewpoint.specification());
    }
  }

  private final ViewpointUseCase useCase;

  ViewpointController(ViewpointUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  List<Response> list() {
    return useCase.list().stream().map(Response::from).toList();
  }

  @GetMapping("/{id}")
  Response get(@PathVariable UUID id) {
    return Response.from(useCase.get(id));
  }

  @PostMapping
  ResponseEntity<Response> create(@Valid @RequestBody Request request) {
    Viewpoint created = useCase.create(request.name(), request.specification());
    return ResponseEntity.created(URI.create("/api/viewpoints/" + created.id()))
        .body(Response.from(created));
  }

  @PutMapping("/{id}")
  Response update(@PathVariable UUID id, @Valid @RequestBody Request request) {
    return Response.from(useCase.update(id, request.name(), request.specification()));
  }

  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@PathVariable UUID id) {
    useCase.delete(id);
    return ResponseEntity.noContent().build();
  }
}
