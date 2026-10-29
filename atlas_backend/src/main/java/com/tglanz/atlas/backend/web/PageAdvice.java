package com.tglanz.atlas.backend.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackageClasses = PageAdvice.class)
class PageAdvice {
  private final PageMetadataFactory metadata;

  private final String gaMeasurementId;
  private final String coffeeUrl;

  PageAdvice(
      PageMetadataFactory metadata,
      @Value("${atlas.analytics.measurement-id:}") String gaMeasurementId,
      @Value("${atlas.coffee-url:}") String coffeeUrl) {
    this.metadata = metadata;
    this.gaMeasurementId = gaMeasurementId.strip();
    this.coffeeUrl = coffeeUrl.strip();
  }

  @ModelAttribute
  void page(
      Model model,
      Authentication authentication,
      HttpServletRequest request,
      HttpServletResponse response) {
    addShell(model.asMap(), authentication, request, response);
    model.addAttribute("meta", metadata.privatePage("Form Atlas"));
    model.addAttribute("gaMeasurementId", gaMeasurementId);
    model.addAttribute("coffeeUrl", coffeeUrl);
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
