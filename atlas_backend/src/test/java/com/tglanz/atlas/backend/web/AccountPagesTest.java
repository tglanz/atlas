package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tglanz.atlas.backend.account.application.port.in.AccountUseCase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AccountPagesTest {
  @Autowired MockMvc mvc;
  @Autowired AccountUseCase accounts;

  @Test
  void nativeRegistrationSignsInAndPersistsAuthenticationAcrossRequests() throws Exception {
    String email = "native-registration-" + UUID.randomUUID() + "@example.com";
    String password = "Native-registration-2046";
    var registration =
        mvc.perform(
                post("/register")
                    .with(csrf())
                    .param("email", email)
                    .param("password", password)
                    .param("confirmPassword", password))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/movements"))
            .andExpect(authenticated())
            .andReturn();

    var account = accounts.findByEmail(email).orElseThrow();
    MockHttpSession session =
        MockHttpSession.class.cast(registration.getRequest().getSession(false));
    assertThat(session).isNotNull();

    mvc.perform(get("/routines").session(session))
        .andExpect(status().isOk())
        .andExpect(authenticated().withUsername(account.id().toString()));
    mvc.perform(get("/api/auth/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(account.id().toString()));
  }

  @Test
  void registrationKeepsTheIntendedRoutineDestination() throws Exception {
    String target = "/routines/add?movement=push_up";
    String login =
        mvc.perform(get("/login").param("returnTo", target))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(login).contains("/register?returnTo=", "name=\"returnTo\"");
    String registration =
        mvc.perform(get("/register").param("returnTo", target))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(registration).contains("value=\"" + target + "\"");

    var response =
        mvc.perform(
                post("/register")
                    .with(csrf())
                    .param("email", "return-to-" + UUID.randomUUID() + "@example.com")
                    .param("password", "A-valid-registration-password")
                    .param("confirmPassword", "A-valid-registration-password")
                    .param("returnTo", target))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(target))
            .andExpect(authenticated())
            .andReturn();
    MockHttpSession session = (MockHttpSession) response.getRequest().getSession(false);
    mvc.perform(get(target).session(session)).andExpect(status().isOk());
  }

  @Test
  void invalidRegistrationRetainsTheDestinationAndRejectsExternalRedirects() throws Exception {
    String target = "/routines/new?movement=push_up";
    String html =
        mvc.perform(
                post("/register")
                    .with(csrf())
                    .param("email", "retry@example.com")
                    .param("password", "password-one")
                    .param("confirmPassword", "password-two")
                    .param("returnTo", target))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(html).contains("value=\"" + target + "\"");
    for (String unsafe :
        new String[] {
          "https://example.com", "//example.com", "/unknown", "/routines\\example.com"
        }) {
      assertThat(AccountPages.safeReturnTo(unsafe)).isEqualTo("/movements");
    }
  }

  @Test
  void invalidRegistrationPreservesEmailWithoutEchoingEitherPassword() throws Exception {
    String email = "native-validation-" + UUID.randomUUID() + "@example.com";
    String password = "Never-echo-this-password";
    String confirmation = "Never-echo-confirmation";
    String html =
        mvc.perform(
                post("/register")
                    .with(csrf())
                    .param("email", email)
                    .param("password", password)
                    .param("confirmPassword", confirmation))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(html)
        .contains(
            "Passwords must match",
            "value=\"" + email + "\"",
            "name=\"password\"",
            "name=\"confirmPassword\"")
        .doesNotContain(password, confirmation);
    assertThat(accounts.findByEmail(email)).isEmpty();
  }
}
