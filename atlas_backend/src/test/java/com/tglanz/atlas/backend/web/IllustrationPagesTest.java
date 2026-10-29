package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tglanz.atlas.backend.avatar.application.port.in.AvatarUseCase;
import com.tglanz.atlas.backend.avatar.domain.Avatar;
import com.tglanz.atlas.backend.generation.application.port.in.GenerationDraft;
import com.tglanz.atlas.backend.generation.application.port.in.GenerationUseCase;
import com.tglanz.atlas.backend.generation.application.port.in.ViewpointUseCase;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations.StageDefinition;
import com.tglanz.atlas.backend.generation.domain.Viewpoint;
import com.tglanz.atlas.backend.movement.application.service.MovementService;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementStage;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "MAINTAINER")
class IllustrationPagesTest {
  @Autowired private MockMvc mvc;
  @MockitoBean private AvatarUseCase avatars;
  @MockitoBean private ViewpointUseCase viewpoints;
  @MockitoBean private GenerationUseCase generations;
  @MockitoBean private MovementService movements;

  private final Avatar avatar = Avatar.create("Athlete", "A person in training clothes");
  private final Viewpoint viewpoint = Viewpoint.create("Side", "A side view").withDefault(true);
  private Movement movement;

  @BeforeEach
  void setUp() {
    movement =
        Movement.create(
            new MovementKey("plank"),
            new MovementContent(
                "Plank",
                "Hold a straight body.",
                List.of(),
                List.of(new MovementStage("Hold", "Keep the body straight")),
                Set.of()));
    when(avatars.list()).thenReturn(List.of(avatar));
    when(viewpoints.list()).thenReturn(List.of(viewpoint));
    when(movements.get(any())).thenReturn(movement);
    when(generations.list("plank")).thenReturn(List.of());
  }

  @Test
  void settingsRenderAccessibleNativeFormsAndPrivateMetadata() throws Exception {
    mvc.perform(get("/settings/illustrations"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Illustrations | Form Atlas")))
        .andExpect(content().string(containsString("noindex,nofollow")))
        .andExpect(content().string(containsString("Athlete")))
        .andExpect(content().string(containsString("name=\"_csrf\"")))
        .andExpect(content().string(containsString("for=\"new-avatar-specification\"")));
  }

  @Test
  void failedSpecificationRetainsTheSubmittedValues() throws Exception {
    when(avatars.create("", "Keep this description"))
        .thenThrow(new IllegalArgumentException("Enter an avatar name"));
    mvc.perform(
            post("/settings/illustrations/avatars")
                .with(csrf())
                .param("name", "")
                .param("specification", "Keep this description"))
        .andExpect(status().isBadRequest())
        .andExpect(view().name("settings/illustrations"))
        .andExpect(content().string(containsString("Enter an avatar name")))
        .andExpect(content().string(containsString("Keep this description")));
  }

  @Test
  void defaultViewpointUsesExistingUseCase() throws Exception {
    mvc.perform(
            post("/settings/illustrations/viewpoints/" + viewpoint.id() + "/default").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/settings/illustrations?tab=viewpoints"));
    verify(viewpoints).makeDefault(viewpoint.id());
  }

  @Test
  void runningGenerationRendersOnlyItsPollablePanel() throws Exception {
    var generation = pendingGeneration();
    generation.start();
    generation.recordProgress(0, "Waiting in the provider queue");
    when(generations.list("plank")).thenReturn(List.of(generation));
    mvc.perform(get("/movements/plank/generation-status"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("data-generation-running=\"true\"")))
        .andExpect(content().string(containsString("Waiting in the provider queue")))
        .andExpect(content().string(not(containsString("<html"))))
        .andExpect(content().string(not(containsString("Save movement"))));
  }

  @Test
  void applyingUpdatesImagesWithoutRenderingTheEditor() throws Exception {
    var generation = pendingGeneration();
    generation.start();
    generation.recordDone(0, "https://example.com/new-plank.png", Instant.now());
    when(generations.list("plank")).thenReturn(List.of(generation));
    doAnswer(
            invocation -> {
              movement.replaceIllustrations(
                  generation.approve().stream().map(Illustration::new).toList());
              return generation;
            })
        .when(generations)
        .apply("plank", generation.id());
    mvc.perform(
            post("/movements/plank/generations/" + generation.id() + "/apply")
                .with(csrf())
                .header("X-Fragment", "true"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Illustrations applied.")))
        .andExpect(content().string(containsString("https://example.com/new-plank.png")))
        .andExpect(content().string(containsString("data-generation-running=\"false\"")))
        .andExpect(content().string(not(containsString("<html"))));
  }

  @Test
  void generationConflictReturnsAnInlineError() throws Exception {
    when(generations.request(eq("plank"), eq(avatar.id()), eq(viewpoint.id()), any(), isNull()))
        .thenThrow(new ConflictException("A generation is already in progress"));
    mvc.perform(
            post("/movements/plank/generations")
                .with(csrf())
                .header("X-Fragment", "true")
                .param("avatarId", avatar.id().toString())
                .param("viewpointId", viewpoint.id().toString())
                .param("name", "Temporary plank")
                .param("guidelines", "Show the fingers clearly")
                .param("stages[0].name", "Temporary hold")
                .param("stages[0].description", "Arms visible in profile"))
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("A generation is already in progress")))
        .andExpect(content().string(containsString("id=\"generation-panel\"")));
  }

  @Test
  void nativeGenerationSubmissionPreservesTheDraftAndReturnsToIllustrations() throws Exception {
    mvc.perform(
            post("/movements/plank/generations")
                .with(csrf())
                .param("avatarId", avatar.id().toString())
                .param("viewpointId", viewpoint.id().toString())
                .param("name", "Temporary plank")
                .param("guidelines", "Show the fingers clearly")
                .param("stages[0].name", "Temporary hold")
                .param("stages[0].description", "Arms visible in profile"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/illustrations?movement=plank"));
    var draft = ArgumentCaptor.forClass(GenerationDraft.class);
    verify(generations)
        .request(eq("plank"), eq(avatar.id()), eq(viewpoint.id()), draft.capture(), isNull());
    assertThat(draft.getValue().name()).isEqualTo("Temporary plank");
    assertThat(draft.getValue().guidelines()).isEqualTo("Show the fingers clearly");
  }

  @Test
  @WithMockUser(roles = "PRACTITIONER")
  void practitionerCannotReadOrChangeGenerationSettings() throws Exception {
    mvc.perform(get("/settings/illustrations")).andExpect(status().isForbidden());
    mvc.perform(get("/illustrations")).andExpect(status().isForbidden());
    mvc.perform(get("/illustrations/plank/status")).andExpect(status().isForbidden());
    mvc.perform(post("/illustrations/plank/generations").with(csrf()))
        .andExpect(status().isForbidden());
    mvc.perform(get("/movements/plank/generation-status")).andExpect(status().isForbidden());
    mvc.perform(
            post("/settings/illustrations/avatars")
                .with(csrf())
                .param("name", "Name")
                .param("specification", "Appearance"))
        .andExpect(status().isForbidden());
  }

  @Test
  void workspaceLoadsEditableInputsAndKeepsTheStatusFragmentSeparate() throws Exception {
    mvc.perform(get("/illustrations").param("movement", "plank"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("data-generation-form")))
        .andExpect(content().string(containsString("Additional guidelines")))
        .andExpect(content().string(containsString("Generate this stage")))
        .andExpect(content().string(containsString("Generate all stages")))
        .andExpect(content().string(containsString("Keep the body straight")))
        .andExpect(content().string(not(containsString("Refresh generation status"))))
        .andExpect(content().string(containsString("/illustrations/plank/status")));
    mvc.perform(get("/illustrations/plank/status"))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("data-generation-form"))))
        .andExpect(content().string(not(containsString("<textarea"))));
  }

  @Test
  void invalidNativeSubmissionKeepsEditedInputs() throws Exception {
    mvc.perform(
            post("/illustrations/plank/generations")
                .with(csrf())
                .param("name", "Draft still here")
                .param("guidelines", "Keep these guidelines")
                .param("stages[0].name", "Hold")
                .param("stages[0].description", "Edited pose"))
        .andExpect(status().isBadRequest())
        .andExpect(view().name("illustrations/workspace"))
        .andExpect(content().string(containsString("Draft still here")))
        .andExpect(content().string(containsString("Keep these guidelines")));
  }

  @Test
  void selectedStageIsPassedWithTheLiveDraft() throws Exception {
    mvc.perform(
            post("/illustrations/plank/generations")
                .with(csrf())
                .header("X-Fragment", "true")
                .param("avatarId", avatar.id().toString())
                .param("viewpointId", viewpoint.id().toString())
                .param("name", "Draft name")
                .param("cues", "First edited cue\nSecond edited cue")
                .param("guidelines", "Front view of hands")
                .param("stagePosition", "0")
                .param("stages[0].name", "Draft hold")
                .param("stages[0].description", "Draft body position"))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("data-generation-form"))));
    var draft = ArgumentCaptor.forClass(GenerationDraft.class);
    verify(generations)
        .request(eq("plank"), eq(avatar.id()), eq(viewpoint.id()), draft.capture(), eq(0));
    assertThat(draft.getValue().cues()).containsExactly("First edited cue", "Second edited cue");
    assertThat(draft.getValue().stages().getFirst().name()).isEqualTo("Draft hold");
  }

  private GenerationMovementIllustrations pendingGeneration() {
    return GenerationMovementIllustrations.create(
        "plank",
        List.of(new StageDefinition(avatar.id(), viewpoint.id(), "Hold", "Keep the body straight")),
        Instant.now());
  }
}
