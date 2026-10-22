package com.tglanz.atlas.backend.account.adapter.in.web;

import com.tglanz.atlas.backend.account.application.port.in.AccountUseCase;
import com.tglanz.atlas.backend.account.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Registration and current user. Login and logout are handled by Spring Security. */
@RestController
@RequestMapping("/api/auth")
class AuthController {

  record RegisterRequest(@NotBlank String email, @NotBlank String password) {}

  record UserResponse(UUID id, String email, String role) {
    static UserResponse from(User user) {
      return new UserResponse(user.id(), user.email(), user.role().name());
    }
  }

  private final AccountUseCase accounts;

  AuthController(AccountUseCase accounts) {
    this.accounts = accounts;
  }

  @PostMapping("/register")
  ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
    User user = accounts.register(request.email(), request.password());
    return ResponseEntity.created(URI.create("/api/auth/me")).body(UserResponse.from(user));
  }

  @GetMapping("/me")
  UserResponse me(Principal principal) {
    return UserResponse.from(accounts.get(UUID.fromString(principal.getName())));
  }
}
