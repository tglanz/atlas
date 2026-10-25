package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.routine.application.port.in.RoutineUseCase;
import com.tglanz.atlas.backend.routine.domain.RoutineContent;
import com.tglanz.atlas.backend.routine.domain.Section;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "routine-render-owner", roles = "PRACTITIONER")
class RoutineRenderingTest {
  @Autowired MockMvc mvc;
  @Autowired RoutineUseCase routines;
  @Autowired MovementQueryUseCase movements;

  @Test
  void newEditorRendersANativePrivateFormWithCsrfAndMovementChoices() throws Exception {
    String html =
        mvc.perform(get("/routines/new"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(html)
        .contains(
            "action=\"/routines/new\"", "name=\"_csrf\"", "name=\"name\"", "data-routine-picker")
        .contains("noindex,nofollow")
        .doesNotContain("<link rel=\"canonical\"");
    assertThat(html).contains(movements.names().getFirst().name());
  }

  @Test
  void savedRoutineRendersCurrentNamesAndEditableOrderedReferences() throws Exception {
    var movement = movements.names().getFirst();
    var routine =
        routines.create(
            "routine-render-owner",
            new RoutineContent(
                "Rendering practice",
                "<script>unsafe()</script>",
                List.of(
                    new Section(
                        "First", "", List.of(movement.key().value(), movement.key().value())),
                    new Section("Second", "", List.of()))));

    String detail =
        mvc.perform(get("/routines/{id}", routine.id()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(detail).contains("Rendering practice", movement.name(), "&lt;script&gt;");
    assertThat(detail).doesNotContain("<script>unsafe()");

    String editor =
        mvc.perform(get("/routines/{id}/edit", routine.id()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(editor)
        .contains(
            "name=\"sections[0].movements[0]\"",
            "name=\"sections[0].movements[1]\"",
            "name=\"sections[1].name\"")
        .contains("value=\"First\"", "value=\"Second\"", "name=\"_csrf\"");
  }

  @Test
  void invalidNativePostRendersNestedSubmittedValuesForCorrection() throws Exception {
    var movement = movements.names().getFirst();
    String html =
        mvc.perform(
                post("/routines/new")
                    .with(csrf())
                    .param("name", "")
                    .param("description", "Keep the description")
                    .param("sections[0].name", "My section")
                    .param("sections[0].description", "Keep these notes")
                    .param("sections[0].movements[0]", movement.key().value()))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(html)
        .contains(
            "Enter a routine name",
            "Keep the description",
            "Keep these notes",
            "value=\"My section\"")
        .contains("name=\"sections[0].movements[0]\"", "value=\"" + movement.key().value() + "\"");
  }

  @Test
  void invalidCollectionIndexesReturnBadRequestInsteadOfServerErrors() throws Exception {
    mvc.perform(
            post("/routines/new")
                .with(csrf())
                .param("name", "Malformed input")
                .param("sections[-1].name", "Invalid negative index"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/routines/new")
                .with(csrf())
                .param("name", "Malformed input")
                .param("sections[1000].name", "Invalid oversized index"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void sparseSectionsRedisplayValidationErrorsAndKeepSubmittedValues() throws Exception {
    String html =
        mvc.perform(
                post("/routines/new")
                    .with(csrf())
                    .param("name", "Sparse input")
                    .param("sections[1].name", "Keep the second section"))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(html).contains("Enter a name for every section", "Keep the second section");
  }
}
