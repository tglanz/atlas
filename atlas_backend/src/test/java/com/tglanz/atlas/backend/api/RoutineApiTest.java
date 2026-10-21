package com.tglanz.atlas.backend.api;

import static com.tglanz.atlas.backend.api.ApiTestSupport.maintainer;
import static com.tglanz.atlas.backend.api.ApiTestSupport.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class RoutineApiTest {

  @Autowired MockMvc mvc;

  static String routine(String name, int revision) {
    return """
        {"name":"%s","description":"d","sections":[
          {"name":"Warm-up","description":"","movements":[{"key":"plank","revision":%d}]},
          {"name":"Main","movements":[]}]}
        """
        .formatted(name, revision);
  }

  void publishPlank() throws Exception {
    mvc.perform(
            post("/api/movements")
                .with(maintainer())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"key\":\"plank\",\"content\":{\"name\":\"Plank\"}}"))
        .andExpect(status().isCreated());
    mvc.perform(post("/api/movements/plank/draft/publish").with(maintainer()))
        .andExpect(status().isOk());
  }

  String create(String subject, String body) throws Exception {
    var result =
        mvc.perform(
                post("/api/routines")
                    .with(user(subject))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isCreated())
            .andReturn();
    String location = result.getResponse().getHeader("Location");
    return location.substring(location.lastIndexOf('/') + 1);
  }

  @Test
  void requiresAuthentication() throws Exception {
    mvc.perform(get("/api/routines")).andExpect(status().isUnauthorized());
  }

  @Test
  void rejectsUnpublishedReferences() throws Exception {
    mvc.perform(
            post("/api/routines")
                .with(user("u1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(routine("R", 1)))
        .andExpect(status().isBadRequest());
    publishPlank();
    mvc.perform(
            post("/api/routines")
                .with(user("u1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(routine("R", 2)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void ownerManagesRoutineWithOrderedSections() throws Exception {
    publishPlank();
    String id = create("u1", routine("Morning", 1));

    mvc.perform(get("/api/routines/" + id).with(user("u1")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Morning"))
        .andExpect(jsonPath("$.sections[0].name").value("Warm-up"))
        .andExpect(jsonPath("$.sections[0].movements[0].revision").value(1))
        .andExpect(jsonPath("$.sections[1].name").value("Main"));

    String updated =
        """
        {"name":"Evening","description":"","sections":[{"name":"Main","movements":[]},{"name":"Warm-up","movements":[{"key":"plank","revision":1}]}]}
        """;
    mvc.perform(
            put("/api/routines/" + id)
                .with(user("u1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updated))
        .andExpect(status().isOk());
    mvc.perform(get("/api/routines/" + id).with(user("u1")))
        .andExpect(jsonPath("$.name").value("Evening"))
        .andExpect(jsonPath("$.sections[0].name").value("Main"))
        .andExpect(jsonPath("$.sections[1].movements[0].key").value("plank"));

    mvc.perform(get("/api/routines").with(user("u1"))).andExpect(jsonPath("$.length()").value(1));
    mvc.perform(delete("/api/routines/" + id).with(user("u1"))).andExpect(status().isNoContent());
    mvc.perform(get("/api/routines/" + id).with(user("u1"))).andExpect(status().isNotFound());
  }

  @Test
  void routinesArePrivateToOwner() throws Exception {
    publishPlank();
    String id = create("u1", routine("Mine", 1));
    mvc.perform(get("/api/routines/" + id).with(user("u2"))).andExpect(status().isNotFound());
    mvc.perform(delete("/api/routines/" + id).with(user("u2"))).andExpect(status().isNotFound());
    mvc.perform(get("/api/routines").with(user("u2"))).andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void routineKeepsExactRevisionAfterNewRevisionPublished() throws Exception {
    publishPlank();
    String id = create("u1", routine("Pinned", 1));
    mvc.perform(post("/api/movements/plank/revisions").with(maintainer()))
        .andExpect(status().isCreated());
    mvc.perform(post("/api/movements/plank/draft/publish").with(maintainer()))
        .andExpect(status().isOk());
    mvc.perform(get("/api/routines/" + id).with(user("u1")))
        .andExpect(jsonPath("$.sections[0].movements[0].revision").value(1));
  }
}
