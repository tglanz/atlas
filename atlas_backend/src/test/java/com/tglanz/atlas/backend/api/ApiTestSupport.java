package com.tglanz.atlas.backend.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

final class ApiTestSupport {
  private ApiTestSupport() {}

  /** A signed-in maintainer with a valid CSRF token. The user id is the principal name. */
  static RequestPostProcessor maintainer() {
    return request ->
        csrf()
            .postProcessRequest(
                SecurityMockMvcRequestPostProcessors.user("maintainer-1")
                    .roles("MAINTAINER")
                    .postProcessRequest(request));
  }

  /** A signed-in practitioner with the given user id and a valid CSRF token. */
  static RequestPostProcessor user(String userId) {
    return request ->
        csrf()
            .postProcessRequest(
                SecurityMockMvcRequestPostProcessors.user(userId)
                    .password("x")
                    .postProcessRequest(request));
  }
}
