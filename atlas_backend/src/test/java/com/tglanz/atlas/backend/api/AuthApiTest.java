package com.tglanz.atlas.backend.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthApiTest {

  @Autowired MockMvc mvc;

  void register(String email, String password) throws Exception {
    mvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
        .andExpect(status().isCreated());
  }

  MockHttpSession login(String email, String password) throws Exception {
    return (MockHttpSession)
        mvc.perform(
                formLogin("/api/auth/login").userParameter("email").user(email).password(password))
            .andExpect(status().isNoContent())
            .andReturn()
            .getRequest()
            .getSession();
  }

  @Test
  void firstUserIsMaintainerOthersArePractitioners() throws Exception {
    register("first@example.com", "password-1");
    register("Second@Example.com", "password-2");

    mvc.perform(get("/api/auth/me").session(login("first@example.com", "password-1")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("MAINTAINER"))
        .andExpect(jsonPath("$.email").value("first@example.com"));
    mvc.perform(get("/api/auth/me").session(login("second@example.com", "password-2")))
        .andExpect(jsonPath("$.role").value("PRACTITIONER"));
  }

  @Test
  void sessionGrantsRoleBasedAccess() throws Exception {
    register("first@example.com", "password-1");
    register("second@example.com", "password-2");
    String movement = "{\"key\":\"plank\",\"content\":{\"name\":\"Plank\"}}";

    mvc.perform(
            post("/api/movements")
                .with(csrf())
                .session(login("second@example.com", "password-2"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(movement))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/movements")
                .with(csrf())
                .session(login("first@example.com", "password-1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(movement))
        .andExpect(status().isCreated());
  }

  @Test
  void wrongPasswordAndUnknownUserAreUnauthorized() throws Exception {
    register("first@example.com", "password-1");
    mvc.perform(
            formLogin("/api/auth/login")
                .userParameter("email")
                .user("first@example.com")
                .password("nope"))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            formLogin("/api/auth/login")
                .userParameter("email")
                .user("who@example.com")
                .password("password-1"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void anonymousIsUnauthorizedNotRedirected() throws Exception {
    mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/routines")).andExpect(status().isUnauthorized());
  }

  @Test
  void unsafeRequestsNeedCsrfToken() throws Exception {
    mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@example.com\",\"password\":\"password-1\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void registrationValidatesInput() throws Exception {
    register("first@example.com", "password-1");
    mvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"FIRST@example.com\",\"password\":\"password-1\"}"))
        .andExpect(status().isConflict());
    mvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"not-an-email\",\"password\":\"password-1\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"b@example.com\",\"password\":\"short\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void logoutEndsSession() throws Exception {
    register("first@example.com", "password-1");
    MockHttpSession session = login("first@example.com", "password-1");
    mvc.perform(post("/api/auth/logout").with(csrf()).session(session))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
  }

  @Test
  void userChoosesPreferredAvatar() throws Exception {
    register("first@example.com", "password-1");
    MockHttpSession session = login("first@example.com", "password-1");
    var created =
        mvc.perform(
                post("/api/avatars")
                    .with(csrf())
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Sketch\",\"specification\":\"spec\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    String avatarId = created.getResponse().getHeader("Location").replaceAll(".*/", "");

    mvc.perform(get("/api/auth/me").session(session))
        .andExpect(jsonPath("$.preferredAvatarId").doesNotExist());
    mvc.perform(
            put("/api/auth/me/avatar")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"avatarId\":\"%s\"}".formatted(avatarId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.preferredAvatarId").value(avatarId));
    mvc.perform(get("/api/auth/me").session(session))
        .andExpect(jsonPath("$.preferredAvatarId").value(avatarId));

    mvc.perform(
            put("/api/auth/me/avatar")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"avatarId\":\"%s\"}".formatted(java.util.UUID.randomUUID())))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put("/api/auth/me/avatar")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"avatarId\":null}"))
        .andExpect(jsonPath("$.preferredAvatarId").doesNotExist());
  }
}
