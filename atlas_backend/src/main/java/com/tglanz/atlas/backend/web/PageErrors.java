package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.shared.error.ConflictException;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackageClasses = PageAdvice.class)
class PageErrors {
  private final PageMetadataFactory metadata;

  PageErrors(PageMetadataFactory metadata) {
    this.metadata = metadata;
  }

  @ExceptionHandler(NotFoundException.class)
  ModelAndView notFound(
      NotFoundException e,
      Authentication authentication,
      HttpServletRequest request,
      HttpServletResponse response) {
    return error(
        HttpStatus.NOT_FOUND, "Page not found", e.getMessage(), authentication, request, response);
  }

  @ExceptionHandler(ConflictException.class)
  ModelAndView conflict(
      ConflictException e,
      Authentication authentication,
      HttpServletRequest request,
      HttpServletResponse response) {
    return error(
        HttpStatus.CONFLICT,
        "Could not complete the action",
        e.getMessage(),
        authentication,
        request,
        response);
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    InvalidPropertyException.class,
    MethodArgumentTypeMismatchException.class,
    BindException.class
  })
  ModelAndView invalid(
      Exception e,
      Authentication authentication,
      HttpServletRequest request,
      HttpServletResponse response) {
    return error(
        HttpStatus.BAD_REQUEST,
        "Check your request",
        "The submitted request is invalid. Please go back and check your entries.",
        authentication,
        request,
        response);
  }

  private ModelAndView error(
      HttpStatus status,
      String title,
      String message,
      Authentication authentication,
      HttpServletRequest request,
      HttpServletResponse response) {
    var view = new ModelAndView("error");
    PageAdvice.addShell(view.getModel(), authentication, request, response);
    view.setStatus(status);
    view.addObject("meta", metadata.privatePage(title));
    view.addObject("errorTitle", title);
    view.addObject("errorMessage", message);
    return view;
  }
}
