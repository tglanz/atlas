package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.MovementInstructions;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementName;
import com.tglanz.atlas.backend.routine.application.port.in.RoutineUseCase;
import com.tglanz.atlas.backend.routine.domain.Routine;
import com.tglanz.atlas.backend.routine.domain.RoutineContent;
import com.tglanz.atlas.backend.routine.domain.Section;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class RoutinePagesTest {
  @Mock RoutineUseCase routines;
  @Mock MovementQueryUseCase movements;
  private MockMvc mvc;

  @BeforeEach
  void setup() {
    var metadata = new PageMetadataFactory("https://atlas.example");
    var validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mvc =
        MockMvcBuilders.standaloneSetup(new RoutinePages(routines, movements, metadata))
            .setControllerAdvice(new PageErrors(metadata))
            .setValidator(validator)
            .build();
  }

  @Test
  void nativeFormPreservesTheOrderAndRepeatedMovementReferences() throws Exception {
    Routine saved = Routine.create("owner", new RoutineContent("Practice", "", List.of()));
    when(routines.create(eq("owner"), any())).thenReturn(saved);

    mvc.perform(
            post("/routines/new")
                .principal(() -> "owner")
                .param("name", "Practice")
                .param("description", "A quiet start")
                .param("sections[0].name", "Warm up")
                .param("sections[0].description", "Start gently")
                .param("sections[0].movements[0]", "squat")
                .param("sections[0].movements[1]", "push-up")
                .param("sections[0].movements[2]", "squat")
                .param("sections[1].name", "Finish")
                .param("sections[1].description", ""))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/routines/" + saved.id()));

    var content = ArgumentCaptor.forClass(RoutineContent.class);
    verify(routines).create(eq("owner"), content.capture());
    assertThat(content.getValue().description()).isEqualTo("A quiet start");
    assertThat(content.getValue().sections())
        .extracting(Section::name)
        .containsExactly("Warm up", "Finish");
    assertThat(content.getValue().sections().getFirst().movements())
        .containsExactly("squat", "push-up", "squat");
  }

  @Test
  void invalidEditRetainsSubmittedNestedValuesAndDoesNotSave() throws Exception {
    Routine existing = Routine.create("owner", new RoutineContent("Existing", "", List.of()));
    when(routines.get("owner", existing.id())).thenReturn(existing);
    when(movements.names())
        .thenReturn(List.of(new MovementName(new MovementKey("squat"), "Squat")));

    var response =
        mvc.perform(
                post("/routines/{id}/edit", existing.id())
                    .principal(() -> "owner")
                    .param("name", "")
                    .param("description", "Do not lose this")
                    .param("sections[0].name", "")
                    .param("sections[0].description", "Keep this too")
                    .param("sections[0].movements[0]", "squat"))
            .andExpect(status().isBadRequest())
            .andExpect(view().name("routines/editor"))
            .andExpect(model().attributeHasFieldErrors("routineForm", "name", "sections[0].name"))
            .andReturn();

    RoutineForm form = (RoutineForm) response.getModelAndView().getModel().get("routineForm");
    assertThat(form.getDescription()).isEqualTo("Do not lose this");
    assertThat(form.getSections().getFirst().getDescription()).isEqualTo("Keep this too");
    assertThat(form.getSections().getFirst().getMovements()).containsExactly("squat");
    verify(routines, never()).update(any(), any(), any());
  }

  @Test
  void invalidEditStillRequiresOwnershipBeforeRenderingTheEditor() throws Exception {
    var id = java.util.UUID.randomUUID();
    when(routines.get("other-owner", id)).thenThrow(new NotFoundException("Routine not found"));

    mvc.perform(
            post("/routines/{id}/edit", id)
                .principal(
                    UsernamePasswordAuthenticationToken.authenticated(
                        "other-owner", null, List.of()))
                .param("name", ""))
        .andExpect(status().isNotFound())
        .andExpect(view().name("error"));

    verify(routines, never()).update(any(), any(), any());
    verify(movements, never()).names();
  }

  @Test
  void missingMovementKeepsTheEntireFormForCorrection() throws Exception {
    when(routines.create(eq("owner"), any()))
        .thenThrow(new IllegalArgumentException("Movement missing does not exist"));
    when(movements.names()).thenReturn(List.of());

    var response =
        mvc.perform(
                post("/routines/new")
                    .principal(() -> "owner")
                    .param("name", "My practice")
                    .param("sections[0].name", "Warm up")
                    .param("sections[0].movements[0]", "missing"))
            .andExpect(status().isBadRequest())
            .andExpect(view().name("routines/editor"))
            .andExpect(model().hasErrors())
            .andReturn();

    RoutineForm form = (RoutineForm) response.getModelAndView().getModel().get("routineForm");
    assertThat(form.getName()).isEqualTo("My practice");
    assertThat(form.getSections().getFirst().getMovements()).containsExactly("missing");
  }

  @Test
  void detailResolvesCurrentMovementInstructionsInOneBatchAndKeepsOrder() throws Exception {
    Routine routine =
        Routine.create(
            "owner",
            new RoutineContent(
                "Practice",
                "",
                List.of(new Section("Main", "", List.of("squat", "push-up", "squat")))));
    when(routines.get("owner", routine.id())).thenReturn(routine);
    when(movements.instructions(List.of(new MovementKey("squat"), new MovementKey("push-up"))))
        .thenReturn(
            List.of(
                new MovementInstructions(
                    new MovementKey("push-up"), "Push Up", List.of(), List.of()),
                new MovementInstructions(
                    new MovementKey("squat"),
                    "Current Squat Name",
                    List.of(
                        new Illustration("/squat-start.png"), new Illustration("/squat-end.png")),
                    List.of("Stay balanced", "Stand tall"))));

    var response =
        mvc.perform(get("/routines/{id}", routine.id()).principal(() -> "owner"))
            .andExpect(status().isOk())
            .andExpect(view().name("routines/detail"))
            .andReturn();
    RoutinePages.RoutineView detail =
        (RoutinePages.RoutineView) response.getModelAndView().getModel().get("routine");
    assertThat(detail.sections().getFirst().movements())
        .extracting(MovementInstructions::name)
        .containsExactly("Current Squat Name", "Push Up", "Current Squat Name");
    var expanded = detail.sections().getFirst().movements();
    assertThat(expanded.getFirst().illustrations())
        .extracting(Illustration::imageUrl)
        .containsExactly("/squat-start.png", "/squat-end.png");
    assertThat(expanded.getFirst().cues()).containsExactly("Stay balanced", "Stand tall");
    assertThat(expanded.get(2)).isEqualTo(expanded.getFirst());
    verify(movements).instructions(List.of(new MovementKey("squat"), new MovementKey("push-up")));
  }

  @Test
  void unavailableMovementKeepsItsPlaceWithoutHidingTheRoutine() throws Exception {
    Routine routine =
        Routine.create(
            "owner",
            new RoutineContent(
                "Practice", "", List.of(new Section("Main", "", List.of("missing")))));
    when(routines.get("owner", routine.id())).thenReturn(routine);
    when(movements.instructions(List.of(new MovementKey("missing")))).thenReturn(List.of());

    var response =
        mvc.perform(get("/routines/{id}", routine.id()).principal(() -> "owner"))
            .andExpect(status().isOk())
            .andReturn();
    RoutinePages.RoutineView detail =
        (RoutinePages.RoutineView) response.getModelAndView().getModel().get("routine");
    assertThat(detail.sections().getFirst().movements())
        .containsExactly(
            new MovementInstructions(new MovementKey("missing"), "missing", List.of(), List.of()));
  }

  @Test
  void deleteDelegatesTheAuthenticatedOwnerAndRedirectsToTheirLibrary() throws Exception {
    var id = java.util.UUID.randomUUID();
    mvc.perform(post("/routines/{id}/delete", id).principal(() -> "owner"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/routines"));
    verify(routines).delete("owner", id);
  }
}
