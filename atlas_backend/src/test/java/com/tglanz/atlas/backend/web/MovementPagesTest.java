package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tglanz.atlas.backend.graph.application.port.in.GraphUseCase;
import com.tglanz.atlas.backend.graph.domain.Edge;
import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.ItemType;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementItem;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementStage;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MovementPagesTest {
  @Autowired private MockMvc mvc;
  @Autowired private MovementCommandUseCase commands;
  @Autowired private MovementQueryUseCase queries;
  @Autowired private GraphUseCase graph;

  private static String key() {
    return "page_" + UUID.randomUUID().toString().replace("-", "");
  }

  private void create(String key, String name) {
    commands.create(
        new MovementKey(key),
        new MovementContent(
            name,
            "An instructional description",
            List.of(new MovementItem(ItemType.CUE, "Keep your body straight")),
            List.of(new MovementStage("Bottom", "Chest near the floor")),
            Set.of("strength")));
  }

  @Test
  void publicPagesRenderContentAndRelationshipsWithoutClientRendering() throws Exception {
    String source = key();
    String target = key();
    create(source, "Rendered movement");
    create(target, "Related progression");
    graph.setOutgoing(source, List.of(new Edge(source, target, "progression")));

    mvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Understand the form.")));
    mvc.perform(get("/movements").param("text", "Rendered movement"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Rendered movement")))
        .andExpect(content().string(containsString("noindex,follow")));
    mvc.perform(get("/movements").param("tags", "strength", "unused-tag"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("unused-tag")));
    mvc.perform(get("/movements/" + source))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Keep your body straight")))
        .andExpect(content().string(containsString("Chest near the floor")))
        .andExpect(content().string(containsString("Progressions")))
        .andExpect(content().string(containsString("Related progression")));
  }

  @Test
  void liveSearchReturnsOnlyTheResultsFragment() throws Exception {
    mvc.perform(
            get("/movements")
                .param("text", "nothing-matches-this-search")
                .header("X-Atlas-Fragment", "movements"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("id=\"movement-results\"")))
        .andExpect(content().string(containsString("No movements found.")))
        .andExpect(
            result ->
                assertThat(result.getResponse().getContentAsString())
                    .doesNotContain("<html", "<head"));
  }

  @Test
  @WithMockUser(roles = "MAINTAINER")
  void creationRejectsTheReservedEditorPath() throws Exception {
    mvc.perform(post("/movements/new").with(csrf()).param("key", "new").param("name", "Reserved"))
        .andExpect(status().isBadRequest())
        .andExpect(model().attributeHasFieldErrors("form", "key"))
        .andExpect(content().string(containsString("new is reserved for movement creation")));
  }

  @Test
  @WithMockUser(roles = "MAINTAINER")
  void nullRelationshipTypeRedisplaysValidationErrorsInsteadOfCrashing() throws Exception {
    String source = key();
    mvc.perform(
            post("/movements/new")
                .with(csrf())
                .param("key", source)
                .param("name", "Draft with an incomplete relationship")
                .param("links[0].target", "some-target")
                .param("_links[0].type", "on"))
        .andExpect(status().isBadRequest())
        .andExpect(model().attributeHasFieldErrors("form", "links[0].type"))
        .andExpect(content().string(containsString("Draft with an incomplete relationship")));
    assertThat(queries.exists(new MovementKey(source))).isFalse();
  }

  @Test
  @WithMockUser(roles = "MAINTAINER")
  void createsMovementAndInverseRelationshipsWithOneSubmission() throws Exception {
    String source = key();
    String target = key();
    create(target, "Relationship target");
    mvc.perform(
            post("/movements/new")
                .with(csrf())
                .param("key", source)
                .param("name", "New saved movement")
                .param("description", "Description")
                .param("cues[0]", "First cue")
                .param("cues[1]", "Second cue")
                .param("comments[0]", "A comment")
                .param("tags[0]", " Push ")
                .param("stages[0].name", "Top")
                .param("stages[0].description", "Arms straight")
                .param("links[0].target", target)
                .param("links[0].type", "progression"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/movements/" + source));

    var movement = queries.get(new MovementKey(source));
    assertThat(movement.content().items())
        .extracting(MovementItem::content)
        .containsExactly("First cue", "Second cue", "A comment");
    assertThat(movement.content().tags()).containsExactly("push");
    assertThat(graph.edgesOf(target).outgoing()).contains(new Edge(target, source, "regression"));
  }

  @Test
  @WithMockUser(roles = "MAINTAINER")
  void failedRelationshipSaveRedisplaysDraftAndRollsBackCreation() throws Exception {
    String source = key();
    mvc.perform(
            post("/movements/new")
                .with(csrf())
                .param("key", source)
                .param("name", "Unsaved movement")
                .param("cues[0]", "Preserve this draft")
                .param("links[0].target", "does_not_exist_" + key())
                .param("links[0].type", "variant"))
        .andExpect(status().isBadRequest())
        .andExpect(model().attributeHasErrors("form"))
        .andExpect(content().string(containsString("Preserve this draft")));
    assertThat(queries.exists(new MovementKey(source))).isFalse();
  }

  @Test
  @WithMockUser(roles = "MAINTAINER")
  void editingPreservesIllustrationsAndAnInvalidGraphChangeDoesNotPublishContent()
      throws Exception {
    String source = key();
    create(source, "Original name");
    commands.replaceIllustrations(
        new MovementKey(source), List.of(new Illustration("https://example.com/step.png")));
    mvc.perform(get("/movements/" + source + "/edit"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Original name")));
    mvc.perform(
            post("/movements/" + source + "/edit")
                .with(csrf())
                .param("key", source)
                .param("name", "Must not publish")
                .param("links[0].target", source)
                .param("links[0].type", "variant"))
        .andExpect(status().isBadRequest());
    assertThat(queries.get(new MovementKey(source)).content().name()).isEqualTo("Original name");

    mvc.perform(
            post("/movements/" + source + "/edit")
                .with(csrf())
                .param("key", source)
                .param("name", "Updated name"))
        .andExpect(status().is3xxRedirection());
    var saved = queries.get(new MovementKey(source));
    assertThat(saved.content().name()).isEqualTo("Updated name");
    assertThat(saved.illustrations())
        .containsExactly(new Illustration("https://example.com/step.png"));
  }

  @Test
  void sparseIllustrationUsesItsOwnStageCaptionAndExportsTheAssociation() throws Exception {
    String value = key();
    commands.create(
        new MovementKey(value),
        new MovementContent(
            "Sparse movement",
            "",
            List.of(),
            List.of(
                new MovementStage("Bottom", "Bottom pose"), new MovementStage("Top", "Top pose")),
            Set.of()));
    commands.replaceIllustrations(
        new MovementKey(value), List.of(new Illustration("https://example.com/top.png", 1)));
    mvc.perform(get("/movements/" + value))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("<span>Top</span>")));
    mvc.perform(get("/api/movements/" + value))
        .andExpect(status().isOk())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(
                    "$.illustrationStagePositions[0]")
                .value(1));
  }
}
