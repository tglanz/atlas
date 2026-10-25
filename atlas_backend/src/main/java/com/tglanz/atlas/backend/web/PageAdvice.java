package com.tglanz.atlas.backend.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackageClasses = PageAdvice.class)
class PageAdvice {
  private final PageMetadataFactory metadata;

  PageAdvice(PageMetadataFactory metadata) {
    this.metadata = metadata;
  }

  @ModelAttribute
  void page(
      Model model,
      Authentication authentication,
      HttpServletRequest request,
      HttpServletResponse response) {
    addShell(model.asMap(), authentication, request, response);
    model.addAttribute("meta", metadata.privatePage("Form Atlas"));
  }

  static void addShell(
      Map<String, Object> model,
      Authentication authentication,
      HttpServletRequest request,
      HttpServletResponse response) {
    boolean signedIn =
        authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    model.put("signedIn", signedIn);
    model.put(
        "maintainer",
        signedIn
            && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MAINTAINER")));
    model.put("currentPath", request.getRequestURI());
    model.put("pageScripts", List.of());
    response.setHeader("Cache-Control", "private, no-store");
  }
}
