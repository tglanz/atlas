package com.tglanz.atlas.backend.avatar.adapter.in.web;

import com.tglanz.atlas.backend.avatar.application.port.in.AvatarUseCase;
import com.tglanz.atlas.backend.avatar.domain.Avatar;
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
@RequestMapping("/api/avatars")
class AvatarController {

  record Request(@NotBlank @Size(max = 100) String name, @NotBlank String specification) {}

  record Response(UUID id, String name, String specification) {
    static Response from(Avatar avatar) {
      return new Response(avatar.id(), avatar.name(), avatar.specification());
    }
  }

  private final AvatarUseCase useCase;

  AvatarController(AvatarUseCase useCase) {
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
    Avatar created = useCase.create(request.name(), request.specification());
    return ResponseEntity.created(URI.create("/api/avatars/" + created.id()))
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
