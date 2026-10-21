package com.tglanz.atlas.backend.api;

import static com.tglanz.atlas.backend.api.ApiTestSupport.maintainer;
import static com.tglanz.atlas.backend.api.ApiTestSupport.user;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
class IllustrationApiTest {

  @Autowired MockMvc mvc;

  static final String BODY = "{\"name\":\"Standard\",\"specification\":\"Sketch style\"}";

  @Test
  void avatarCrud() throws Exception {
    var created =
        mvc.perform(
                post("/api/avatars")
                    .with(maintainer())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(BODY))
            .andExpect(status().isCreated())
            .andReturn();
    String location = created.getResponse().getHeader("Location");

    mvc.perform(get(location))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Standard"));
    mvc.perform(get("/api/avatars")).andExpect(jsonPath("$.length()").value(1));
    mvc.perform(
            put(location)
                .with(maintainer())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Renamed\",\"specification\":\"x\"}"))
        .andExpect(jsonPath("$.name").value("Renamed"));
    String other = createAvatar("Other");
    mvc.perform(delete(other).with(maintainer())).andExpect(status().isNoContent());
    mvc.perform(get(other)).andExpect(status().isNotFound());
  }

  @Test
  void specificationIsVisibleToMaintainersOnly() throws Exception {
    String avatar = createAvatar("Sketch");
    mvc.perform(get(avatar))
        .andExpect(jsonPath("$.name").value("Sketch"))
        .andExpect(jsonPath("$.specification").doesNotExist());
    mvc.perform(get("/api/avatars")).andExpect(jsonPath("$[0].specification").doesNotExist());
    mvc.perform(get(avatar).with(user("u"))).andExpect(jsonPath("$.specification").doesNotExist());
    mvc.perform(get(avatar).with(maintainer()))
        .andExpect(jsonPath("$.specification").value("spec"));
  }

  @Test
  void viewpointsAreMaintainerOnly() throws Exception {
    mvc.perform(get("/api/viewpoints")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/viewpoints").with(user("u"))).andExpect(status().isForbidden());
    mvc.perform(get("/api/viewpoints").with(maintainer())).andExpect(status().isOk());
  }

  @Test
  void duplicateNameIsConflict() throws Exception {
    mvc.perform(
            post("/api/viewpoints")
                .with(maintainer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/viewpoints")
                .with(maintainer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isConflict());
  }

  @Test
  void writesRequireMaintainer() throws Exception {
    mvc.perform(
            post("/api/avatars").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/viewpoints")
                .with(user("u"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isForbidden());
  }

  String createAvatar(String name) throws Exception {
    var result =
        mvc.perform(
                post("/api/avatars")
                    .with(maintainer())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"%s\",\"specification\":\"spec\"}".formatted(name)))
            .andExpect(status().isCreated())
            .andReturn();
    return result.getResponse().getHeader("Location");
  }

  @Test
  void firstAvatarIsDefaultAndDefaultCanMove() throws Exception {
    String sketch = createAvatar("Sketch");
    String cartoon = createAvatar("Cartoon");

    mvc.perform(get(sketch)).andExpect(jsonPath("$.isDefault").value(true));
    mvc.perform(get(cartoon)).andExpect(jsonPath("$.isDefault").value(false));

    mvc.perform(put(cartoon + "/default").with(maintainer()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isDefault").value(true));
    mvc.perform(get(sketch)).andExpect(jsonPath("$.isDefault").value(false));
    mvc.perform(get("/api/avatars")).andExpect(jsonPath("$[?(@.isDefault == true)]", hasSize(1)));
  }

  @Test
  void defaultAvatarCannotBeDeleted() throws Exception {
    String sketch = createAvatar("Sketch");
    createAvatar("Cartoon");
    mvc.perform(delete(sketch).with(maintainer())).andExpect(status().isConflict());
  }

  @Test
  void onlyMaintainersChangeTheDefault() throws Exception {
    String sketch = createAvatar("Sketch");
    mvc.perform(put(sketch + "/default").with(user("u"))).andExpect(status().isForbidden());
  }
}
