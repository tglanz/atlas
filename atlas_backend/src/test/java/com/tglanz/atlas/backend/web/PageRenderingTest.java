package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tglanz.atlas.backend.account.application.port.in.AccountUseCase;
import com.tglanz.atlas.backend.graph.application.port.in.GraphUseCase;
import com.tglanz.atlas.backend.graph.domain.Edge;
import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "atlas.site-url=https://atlas.example")
@AutoConfigureMockMvc
class PageRenderingTest {
  @Autowired MockMvc mvc;
  @Autowired MovementCommandUseCase commands;
  @Autowired MovementQueryUseCase movements;
  @Autowired GraphUseCase graph;
  @Autowired AccountUseCase accounts;

  @Test
  void publicHtmlIncludesCurrentContentRelatedLinksAndServerMetadata() throws Exception {
    String key = key();
    String related = key();
    commands.create(
        new MovementKey(key), movementContent("Original movement", "Keep the body straight."));
    commands.create(
        new MovementKey(related), movementContent("Related movement", "A progression."));
    graph.setOutgoing(key, List.of(new Edge(key, related, "progression")));
    mvc.perform(get("/movements/" + key).header("Host", "untrusted.example"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(content().string(containsString("Original movement | Form Atlas")))
        .andExpect(content().string(containsString("Keep the body straight.")))
        .andExpect(content().string(containsString("Related movement")))
        .andExpect(content().string(containsString("https://atlas.example/movements/" + key)))
        .andExpect(content().string(containsString("application/ld+json")))
        .andExpect(content().string(not(containsString("untrusted.example"))));
    commands.update(
        new MovementKey(key), movementContent("Updated movement", "Fresh database content."));
    mvc.perform(get("/movements/" + key))
        .andExpect(content().string(containsString("Updated movement | Form Atlas")))
        .andExpect(content().string(containsString("Fresh database content.")));
    mvc.perform(get("/sitemap.xml"))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
        .andExpect(content().string(containsString("https://atlas.example/movements/" + key)))
        .andExpect(content().string(not(containsString("<lastmod>"))))
        .andExpect(content().string(not(containsString("/routines"))));
  }

  @Test
  void indexFiltersHaveReloadableUrlsAndServerRenderedResults() throws Exception {
    String key = key();
    commands.create(new MovementKey(key), movementContent("Searchable " + key, "Description."));
    mvc.perform(get("/movements").param("text", key).param("tags", "strength", "strength"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Searchable " + key)))
        .andExpect(content().string(containsString("noindex,follow")))
        .andExpect(content().string(containsString("name=\"tags\"")));
    mvc.perform(get("/movements").header("X-Atlas-Fragment", "movements"))
        .andExpect(status().isOk())
        .andExpect(header().string("Vary", containsString("X-Atlas-Fragment")))
        .andExpect(content().string(not(containsString("<html"))));
  }

  @Test
  void nativeMovementFormsEnforceAuthorizationCsrfAndAtomicSave() throws Exception {
    mvc.perform(get("/movements/new")).andExpect(status().is3xxRedirection());
    mvc.perform(get("/movements/new").with(user("practitioner").roles("PRACTITIONER")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/movements/new").with(user("maintainer").roles("MAINTAINER")))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("name=\"_csrf\"")));
    String key = key();
    mvc.perform(
            post("/movements/new")
                .with(user("maintainer").roles("MAINTAINER"))
                .param("key", key)
                .param("name", "New movement"))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/movements/new")
                .with(user("maintainer").roles("MAINTAINER"))
                .with(csrf())
                .param("key", key)
                .param("name", "New movement")
                .param("description", "Preserve this draft")
                .param("links[0].target", "missing-movement")
                .param("links[0].type", "progression"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Preserve this draft")));
    assertThat(movements.exists(new MovementKey(key))).isFalse();
    mvc.perform(
            post("/movements/new")
                .with(user("maintainer").roles("MAINTAINER"))
                .with(csrf())
                .param("key", key)
                .param("name", "New movement")
                .param("description", "Saved description"))
        .andExpect(redirectedUrl("/movements/" + key));
    mvc.perform(get("/movements/" + key + "/edit").with(user("maintainer").roles("MAINTAINER")))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Saved description")))
        .andExpect(content().string(containsString("generation-panel")));
  }

  @Test
  void htmlAndApiErrorsRemainSeparate() throws Exception {
    mvc.perform(get("/movements/nonexistent-test-movement"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(content().string(containsString("Page not found")));
    mvc.perform(get("/api/movements/nonexistent-test-movement"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    mvc.perform(get("/routines")).andExpect(redirectedUrl("/login?returnTo=%2Froutines"));
    mvc.perform(get("/api/routines")).andExpect(status().isUnauthorized());
  }

  @Test
  void htmlLoginRedirectsLocallyWhileApiLoginKeepsItsStatusContract() throws Exception {
    String email = UUID.randomUUID() + "@example.com";
    accounts.ensureMaintainer(email, "password123");
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .accept(MediaType.TEXT_HTML)
                .param("email", email)
                .param("password", "password123")
                .param("returnTo", "//evil.example"))
        .andExpect(redirectedUrl("/movements"));
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .param("email", email)
                .param("password", "password123"))
        .andExpect(status().isNoContent());
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .accept(MediaType.TEXT_HTML)
                .param("email", email)
                .param("password", "wrong"))
        .andExpect(redirectedUrl("/login?error&returnTo=%2Fmovements"));
  }

  private static String key() {
    return "test-" + UUID.randomUUID().toString().replace("-", "");
  }

  private static MovementContent movementContent(String name, String description) {
    return new MovementContent(
        name,
        description,
        List.of(new MovementItem(ItemType.CUE, "Engage your core.")),
        List.of(new MovementStage("Hold", "A straight body.")),
        Set.of("strength"));
  }
}
