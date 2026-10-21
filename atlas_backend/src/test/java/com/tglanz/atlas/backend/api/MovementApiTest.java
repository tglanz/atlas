package com.tglanz.atlas.backend.api;

import static com.tglanz.atlas.backend.api.ApiTestSupport.maintainer;
import static com.tglanz.atlas.backend.api.ApiTestSupport.user;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MovementApiTest {

  @Autowired MockMvc mvc;

  static final String CREATE =
      """
      {"key":"plank","content":{"name":"Plank","description":"Hold","cues":["Brace","Breathe"],
       "tags":["Core"],"links":[{"target":"side-plank","relationType":"Progression"}]}}
      """;

  static final String UPDATE =
      """
      {"name":"Front plank","description":"Hold longer","cues":["Brace"],"tags":["core","isometric"],"links":[]}
      """;

  void createAndPublish() throws Exception {
    mvc.perform(
            post("/api/movements")
                .with(maintainer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.revision").value(1))
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.cues[1]").value("Breathe"))
        .andExpect(jsonPath("$.links[0].relationType").value("progression"));
    mvc.perform(post("/api/movements/plank/draft/publish").with(maintainer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PUBLISHED"));
  }

  @Test
  void writesRequireMaintainer() throws Exception {
    mvc.perform(
            post("/api/movements")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/movements")
                .with(user("someone"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE))
        .andExpect(status().isForbidden());
  }

  @Test
  void draftsAreHiddenFromEveryoneButMaintainers() throws Exception {
    mvc.perform(
            post("/api/movements")
                .with(maintainer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE))
        .andExpect(status().isCreated());
    mvc.perform(get("/api/movements/plank")).andExpect(status().isNotFound());
    mvc.perform(get("/api/movements"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
    mvc.perform(get("/api/movements/plank/revisions/1")).andExpect(status().isNotFound());
    mvc.perform(get("/api/movements/plank/draft")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/movements/plank/draft").with(user("someone")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/movements/plank/draft").with(maintainer())).andExpect(status().isOk());
  }

  @Test
  void duplicateKeyIsConflict() throws Exception {
    createAndPublish();
    mvc.perform(
            post("/api/movements")
                .with(maintainer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE))
        .andExpect(status().isConflict());
  }

  @Test
  void invalidInputIsBadRequest() throws Exception {
    mvc.perform(
            post("/api/movements")
                .with(maintainer())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"key\":\"Bad Key\",\"content\":{\"name\":\"x\"}}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/movements")
                .with(maintainer())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"key\":\"ok\",\"content\":{\"name\":\"\"}}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void revisionLifecyclePreservesHistory() throws Exception {
    createAndPublish();
    mvc.perform(get("/api/movements/plank"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Plank"));
    mvc.perform(
            put("/api/movements/plank/draft")
                .with(maintainer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE))
        .andExpect(status().isNotFound());

    mvc.perform(post("/api/movements/plank/revisions").with(maintainer()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.revision").value(2))
        .andExpect(jsonPath("$.status").value("DRAFT"));
    mvc.perform(post("/api/movements/plank/revisions").with(maintainer()))
        .andExpect(status().isConflict());
    mvc.perform(
            put("/api/movements/plank/draft")
                .with(maintainer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Front plank"));

    mvc.perform(get("/api/movements/plank")).andExpect(jsonPath("$.revision").value(1));
    mvc.perform(post("/api/movements/plank/draft/publish").with(maintainer()))
        .andExpect(status().isOk());

    mvc.perform(get("/api/movements/plank"))
        .andExpect(jsonPath("$.revision").value(2))
        .andExpect(jsonPath("$.name").value("Front plank"));
    mvc.perform(get("/api/movements/plank/revisions/1"))
        .andExpect(jsonPath("$.name").value("Plank"));
    mvc.perform(get("/api/movements/plank/revisions"))
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].revision").value(1))
        .andExpect(jsonPath("$[1].revision").value(2));
    mvc.perform(get("/api/movements/plank/draft").with(maintainer()))
        .andExpect(status().isNotFound());
  }

  @Test
  void searchFiltersByNameAndTag() throws Exception {
    createAndPublish();
    mvc.perform(get("/api/movements").param("text", "lan"))
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(get("/api/movements").param("text", "squat"))
        .andExpect(jsonPath("$.length()").value(0));
    mvc.perform(get("/api/movements").param("tag", "Core"))
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(get("/api/movements").param("tag", "legs"))
        .andExpect(jsonPath("$.length()").value(0));
  }
}
