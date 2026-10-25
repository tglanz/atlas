package com.tglanz.atlas.backend.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ErrorPagesTest {
  @Autowired MockMvc mvc;

  @Test
  void directErrorRouteDoesNotReturnASuccessStatus() throws Exception {
    mvc.perform(get("/error"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
  }

  @Test
  void browserPageErrorPreservesItsHttpStatus() throws Exception {
    mvc.perform(
            get("/error")
                .with(
                    request -> {
                      request.setDispatcherType(DispatcherType.ERROR);
                      return request;
                    })
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/missing-page"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(content().string(containsString("The page you requested could not be found.")));
  }

  @Test
  void apiErrorRemainsAProblemDetailEvenWhenHtmlIsAccepted() throws Exception {
    mvc.perform(
            get("/error")
                .with(
                    request -> {
                      request.setDispatcherType(DispatcherType.ERROR);
                      return request;
                    })
                .accept(MediaType.TEXT_HTML, MediaType.APPLICATION_PROBLEM_JSON)
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/api/avatars"))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void browserLoginCsrfFailureGetsAnHtmlErrorPage() throws Exception {
    mvc.perform(
            get("/error")
                .with(
                    request -> {
                      request.setDispatcherType(DispatcherType.ERROR);
                      return request;
                    })
                .accept(MediaType.TEXT_HTML)
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/api/auth/login"))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(
            content().string(containsString("You do not have permission to access this page.")));
  }

  @Test
  void apiLoginCsrfFailureKeepsItsProblemDetail() throws Exception {
    mvc.perform(
            get("/error")
                .with(
                    request -> {
                      request.setDispatcherType(DispatcherType.ERROR);
                      return request;
                    })
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/api/auth/login"))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(403));
  }

  @Test
  void exactApiRootKeepsTheApiAuthenticationStatus() throws Exception {
    mvc.perform(get("/api").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void authenticatedNotFoundPageKeepsThePractitionerShell() throws Exception {
    mvc.perform(
            get("/movements/missing-authenticated-page").with(user("owner").roles("PRACTITIONER")))
        .andExpect(status().isNotFound())
        .andExpect(model().attribute("signedIn", true))
        .andExpect(model().attribute("maintainer", false))
        .andExpect(model().attribute("currentPath", "/movements/missing-authenticated-page"))
        .andExpect(header().string("Cache-Control", "private, no-store"))
        .andExpect(content().string(containsString("Sign out")));
  }

  @Test
  void authenticatedNotFoundPageKeepsMaintainerNavigation() throws Exception {
    mvc.perform(
            get("/movements/missing-maintainer-page").with(user("maintainer").roles("MAINTAINER")))
        .andExpect(status().isNotFound())
        .andExpect(model().attribute("signedIn", true))
        .andExpect(model().attribute("maintainer", true))
        .andExpect(content().string(containsString("href=\"/settings/illustrations\"")));
  }
}
