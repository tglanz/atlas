package com.tglanz.atlas.backend.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
class ErrorPages implements ErrorController {
  private final PageMetadataFactory metadata;

  ErrorPages(PageMetadataFactory metadata) {
    this.metadata = metadata;
  }

  @RequestMapping("/error")
  Object error(HttpServletRequest request, HttpServletResponse response, Model model) {
    Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    int status = code instanceof Integer value ? value : 500;
    String original = String.valueOf(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
    HttpStatus resolved = HttpStatus.resolve(status);
    String title = resolved == null ? "Request failed" : resolved.getReasonPhrase();
    String accept = request.getHeader("Accept");
    boolean browserAuth =
        (original.equals("/api/auth/login") || original.equals("/api/auth/logout"))
            && accept != null
            && accept.contains("text/html");
    if ((original.equals("/api") || original.startsWith("/api/")) && !browserAuth)
      return ResponseEntity.status(status).body(ProblemDetail.forStatus(status));
    response.setStatus(status);
    model.addAttribute("meta", metadata.privatePage(title));
    model.addAttribute("errorTitle", title);
    model.addAttribute(
        "errorMessage",
        status == 404
            ? "The page you requested could not be found."
            : status == 403
                ? "You do not have permission to access this page."
                : "The request could not be completed. Please try again.");
    return "error";
  }
}
