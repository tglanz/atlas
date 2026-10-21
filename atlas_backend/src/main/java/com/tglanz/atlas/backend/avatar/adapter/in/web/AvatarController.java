package com.tglanz.atlas.backend.avatar.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tglanz.atlas.backend.avatar.application.port.in.AvatarUseCase;
import com.tglanz.atlas.backend.avatar.domain.Avatar;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

  /** The specification is the generation prompt and is shown to maintainers only. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  record Response(UUID id, String name, String specification, boolean isDefault) {
    static Response from(Avatar avatar, boolean includeSpecification) {
      return new Response(
          avatar.id(),
          avatar.name(),
          includeSpecification ? avatar.specification() : null,
          avatar.isDefault());
    }
  }

  private static boolean isMaintainer(Authentication authentication) {
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_MAINTAINER"));
  }

  private final AvatarUseCase useCase;

  AvatarController(AvatarUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  List<Response> list(Authentication authentication) {
    boolean full = isMaintainer(authentication);
    return useCase.list().stream().map(a -> Response.from(a, full)).toList();
  }

  @GetMapping("/{id}")
  Response get(@PathVariable UUID id, Authentication authentication) {
    return Response.from(useCase.get(id), isMaintainer(authentication));
  }

  @PostMapping
  ResponseEntity<Response> create(@Valid @RequestBody Request request) {
    Avatar created = useCase.create(request.name(), request.specification());
    return ResponseEntity.created(URI.create("/api/avatars/" + created.id()))
        .body(Response.from(created, true));
  }

  @PutMapping("/{id}")
  Response update(@PathVariable UUID id, @Valid @RequestBody Request request) {
    return Response.from(useCase.update(id, request.name(), request.specification()), true);
  }

  @PutMapping("/{id}/default")
  Response makeDefault(@PathVariable UUID id) {
    return Response.from(useCase.makeDefault(id), true);
  }

  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@PathVariable UUID id) {
    useCase.delete(id);
    return ResponseEntity.noContent().build();
  }
}
