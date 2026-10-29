package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.routine.application.port.in.RoutineUseCase;
import com.tglanz.atlas.backend.routine.domain.RoutineContent;
import com.tglanz.atlas.backend.routine.domain.Section;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "routine-add-owner", roles = "PRACTITIONER")
class RoutineAddMovementTest {
  @Autowired MockMvc mvc;
  @Autowired RoutineUseCase routines;
  @Autowired MovementQueryUseCase movements;

  @Test
  void chooserShowsOnlyOwnedRoutinesAndNewRoutineStartsWithTheSelectedMovement() throws Exception {
    String ownName = "Owned choice " + UUID.randomUUID();
    String otherName = "Private choice " + UUID.randomUUID();
    routines.create("routine-add-owner", new RoutineContent(ownName, "", List.of()));
    routines.create("another-owner", new RoutineContent(otherName, "", List.of()));
    String key = movements.names().getFirst().key().value();
    String chooser =
        mvc.perform(get("/routines/add").param("movement", key))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(chooser).contains(ownName, "Create a new routine instead").doesNotContain(otherName);
    String editor =
        mvc.perform(get("/routines/new").param("movement", key))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(editor)
        .contains(
            "name=\"sections[0].movements[0]\"", "value=\"" + key + "\"", "value=\"Practice\"");
  }

  @Test
  void addingToASectionPreservesOtherSectionsAndRepeatedReferences() throws Exception {
    var choices = movements.names();
    String first = choices.get(0).key().value();
    String second = choices.get(1).key().value();
    var routine =
        routines.create(
            "routine-add-owner",
            new RoutineContent(
                "Append practice",
                "Keep notes",
                List.of(
                    new Section("Prepare", "Keep section notes", List.of(first)),
                    new Section("Strength", "", List.of(second)))));
    mvc.perform(
            post("/routines/{id}/add-movement", routine.id())
                .with(csrf())
                .param("movement", first)
                .param("section", "0"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/routines/" + routine.id()));
    var content = routines.get("routine-add-owner", routine.id()).content();
    assertThat(content.description()).isEqualTo("Keep notes");
    assertThat(content.sections().getFirst().description()).isEqualTo("Keep section notes");
    assertThat(content.sections().getFirst().movements()).containsExactly(first, first);
    assertThat(content.sections().getLast().movements()).containsExactly(second);
  }

  @Test
  void addingToAnEmptyRoutineCreatesItsFirstSection() throws Exception {
    String key = movements.names().getFirst().key().value();
    var routine =
        routines.create("routine-add-owner", new RoutineContent("Empty practice", "", List.of()));
    mvc.perform(
            post("/routines/{id}/add-movement", routine.id())
                .with(csrf())
                .param("movement", key)
                .param("section", "0"))
        .andExpect(status().is3xxRedirection());
    assertThat(routines.get("routine-add-owner", routine.id()).content().sections())
        .containsExactly(new Section("Practice", "", List.of(key)));
  }

  @Test
  void invalidAddsAndOtherOwnersCannotChangeTheRoutine() throws Exception {
    String key = movements.names().getFirst().key().value();
    var original =
        new RoutineContent(
            "Protected practice", "", List.of(new Section("Main", "", List.of(key))));
    var routine = routines.create("routine-add-owner", original);
    mvc.perform(
            post("/routines/{id}/add-movement", routine.id())
                .with(csrf())
                .param("movement", key)
                .param("section", "5"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/routines/{id}/add-movement", routine.id())
                .with(csrf())
                .param("movement", "does_not_exist")
                .param("section", "0"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/routines/{id}/add-movement", routine.id())
                .with(csrf())
                .with(user("another-owner").roles("PRACTITIONER"))
                .param("movement", key)
                .param("section", "0"))
        .andExpect(status().isNotFound());
    assertThat(routines.get("routine-add-owner", routine.id()).content()).isEqualTo(original);
  }
}
