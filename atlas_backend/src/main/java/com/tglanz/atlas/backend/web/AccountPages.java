package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.account.application.port.in.AccountUseCase;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class AccountPages {
  private final AccountUseCase accounts;
  private final PageMetadataFactory metadata;

  AccountPages(AccountUseCase accounts, PageMetadataFactory metadata) {
    this.accounts = accounts;
    this.metadata = metadata;
  }

  @GetMapping("/login")
  String login(@RequestParam(defaultValue = "") String returnTo, Model model) {
    model.addAttribute("meta", metadata.privatePage("Sign in"));
    model.addAttribute("returnTo", safeReturnTo(returnTo));
    return "account/login";
  }

  @GetMapping("/register")
  String register(@RequestParam(defaultValue = "") String returnTo, Model model) {
    model.addAttribute("meta", metadata.privatePage("Register"));
    model.addAttribute("email", "");
    model.addAttribute("returnTo", safeReturnTo(returnTo));
    return "account/register";
  }

  @PostMapping("/register")
  String register(
      @RequestParam(defaultValue = "") String email,
      @RequestParam(defaultValue = "") String password,
      @RequestParam(defaultValue = "") String confirmPassword,
      @RequestParam(defaultValue = "") String returnTo,
      Model model,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException {
    model.addAttribute("meta", metadata.privatePage("Register"));
    model.addAttribute("email", email);
    model.addAttribute("returnTo", safeReturnTo(returnTo));
    try {
      if (!password.equals(confirmPassword))
        throw new IllegalArgumentException("Passwords must match");
      var user = accounts.register(email, password);
      request.login(user.email(), password);
      return "redirect:" + safeReturnTo(returnTo);
    } catch (IllegalArgumentException | ConflictException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      model.addAttribute("error", e.getMessage());
      return "account/register";
    }
  }

  public static String safeReturnTo(String path) {
    if (path == null
        || !path.startsWith("/")
        || path.startsWith("//")
        || path.contains("\\")
        || path.contains("\r")
        || path.contains("\n")) return "/movements";
    // Restrict redirects to application routes; never reflect arbitrary redirect targets.
    return path.matches("/(?:movements|routines|settings)(?:[/?].*)?") ? path : "/movements";
  }
}
