package com.tglanz.atlas.backend.generation.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tglanz.atlas.backend.avatar.application.port.in.AvatarUseCase;
import com.tglanz.atlas.backend.generation.application.port.in.*;
import com.tglanz.atlas.backend.generation.application.port.out.*;
import com.tglanz.atlas.backend.generation.domain.*;
import com.tglanz.atlas.backend.movement.application.port.in.*;
import com.tglanz.atlas.backend.movement.domain.*;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class GenerationWorkspaceTest {
  @Autowired GenerationUseCase generations;
  @Autowired RunGenerationUseCase worker;
  @Autowired MovementCommandUseCase commands;
  @Autowired MovementQueryUseCase queries;
  @Autowired AvatarUseCase avatars;
  @Autowired ViewpointUseCase viewpoints;
  @MockitoBean GenerationScheduler scheduler;
  @MockitoBean ImageConnector connector;
  MovementKey key;
  UUID avatarId;
  UUID viewpointId;
  MovementContent original;

  @BeforeEach
  void setup() {
    String suffix = UUID.randomUUID().toString().replace("-", "");
    key = new MovementKey("generation_" + suffix);
    original =
        new MovementContent(
            "Saved name",
            "Saved description",
            List.of(new MovementItem(ItemType.CUE, "Saved cue")),
            List.of(
                new MovementStage("Bottom", "Saved bottom"), new MovementStage("Top", "Saved top")),
            Set.of());
    commands.create(key, original);
    avatarId = avatars.create("Avatar " + suffix, "Original avatar style").id();
    viewpointId = viewpoints.create("View " + suffix, "Original camera angle").id();
    when(connector.submit(any()))
        .thenReturn(
            new ImageConnector.Submission("test", "model", "request", "status", "result", null));
    when(connector.awaitImageUrl(any(), any()))
        .thenReturn("https://images.example/first.png", "https://images.example/second.png");
  }

  GenerationDraft draft() {
    return new GenerationDraft(
        "Draft name",
        "Draft description",
        List.of("Draft cue"),
        List.of(
            new GenerationDraft.Stage("Draft bottom", "Edited bottom pose"),
            new GenerationDraft.Stage("Draft top", "Edited top pose")),
        "Show hands clearly");
  }

  GenerationMovementIllustrations request(Integer stage) {
    return generations.request(key.value(), avatarId, viewpointId, draft(), stage);
  }

  GenerationMovementIllustrations run(Integer stage) {
    var generation = request(stage);
    worker.run(generation.id());
    return generations.get(key.value(), generation.id());
  }

  @Test
  void capturesDraftAndPresetsBeforeTheWorkerRunsWithoutSavingMovementEdits() {
    var generation = request(1);
    assertThat(queries.get(key).content()).isEqualTo(original);
    avatars.update(avatarId, "Changed avatar", "Later avatar style");
    viewpoints.update(viewpointId, "Changed view", "Later camera angle");
    commands.update(
        key,
        new MovementContent(
            "Later saved name", "Later saved description", List.of(), original.stages(), Set.of()));
    worker.run(generation.id());
    var prompt = ArgumentCaptor.forClass(ImageRequest.class);
    verify(connector).submit(prompt.capture());
    assertThat(prompt.getValue().systemPrompt())
        .contains(
            "Draft name",
            "Draft description",
            "Draft cue",
            "Show hands clearly",
            "Original avatar style",
            "Original camera angle")
        .doesNotContain("Later saved", "Later avatar", "Later camera");
    assertThat(prompt.getValue().imagePrompt())
        .contains("Draft top", "Edited top pose")
        .doesNotContain("Edited bottom pose");
    var restored = generations.get(key.value(), generation.id());
    assertThat(restored.input().guidelines()).isEqualTo("Show hands clearly");
    assertThat(restored.input().cues()).containsExactly("Draft cue");
    assertThat(restored.targetStagePosition()).isEqualTo(1);
    assertThat(queries.get(key).content().name()).isEqualTo("Later saved name");
    assertThat(queries.get(key).illustrations()).isEmpty();
  }

  @Test
  void approvingOneStagePreservesTheOtherImage() {
    commands.replaceIllustrations(
        key,
        List.of(
            new Illustration("https://images.example/old0.png", 0),
            new Illustration("https://images.example/old1.png", 1)));
    var generation = run(1);
    assertThat(generation.status()).isEqualTo(GenerationStatus.READY_FOR_REVIEW);
    generations.apply(key.value(), generation.id());
    assertThat(queries.get(key).illustrations())
        .containsExactly(
            new Illustration("https://images.example/old0.png", 0),
            new Illustration("https://images.example/first.png", 1));
    assertThat(queries.get(key).content()).isEqualTo(original);
  }

  @Test
  void canGenerateLaterStageFirstAndFillEarlierStageWithoutLosingTheAssociation() {
    var later = run(1);
    generations.apply(key.value(), later.id());
    assertThat(queries.get(key).illustrations())
        .containsExactly(new Illustration("https://images.example/first.png", 1));
    var earlier = run(0);
    generations.apply(key.value(), earlier.id());
    assertThat(queries.get(key).illustrations())
        .containsExactly(
            new Illustration("https://images.example/second.png", 0),
            new Illustration("https://images.example/first.png", 1));
  }

  @Test
  void allStagesAreGeneratedAndAppliedInOrder() {
    var generation = run(null);
    assertThat(generation.stages()).hasSize(2);
    verify(connector, times(2)).submit(any());
    generations.apply(key.value(), generation.id());
    assertThat(queries.get(key).illustrations())
        .containsExactly(
            new Illustration("https://images.example/first.png", 0),
            new Illustration("https://images.example/second.png", 1));
  }

  @Test
  void refusesToApplyWhenSavedStagesHaveChanged() {
    var generation = run(1);
    commands.update(
        key,
        new MovementContent(
            original.name(),
            original.description(),
            original.items(),
            List.of(new MovementStage("Different", "Different pose")),
            Set.of()));
    assertThatThrownBy(() -> generations.apply(key.value(), generation.id()))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("stages changed");
    assertThat(queries.get(key).illustrations()).isEmpty();
    assertThat(generations.get(key.value(), generation.id()).status())
        .isEqualTo(GenerationStatus.READY_FOR_REVIEW);
  }

  @Test
  void rejectsInvalidStageAndMismatchedStageCountBeforeScheduling() {
    assertThatThrownBy(() -> request(2)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> request(-1)).isInstanceOf(IllegalArgumentException.class);
    var shortDraft =
        new GenerationDraft("Name", "", List.of(), List.of(draft().stages().getFirst()), "");
    assertThatThrownBy(
            () -> generations.request(key.value(), avatarId, viewpointId, shortDraft, null))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(scheduler, connector);
  }

  @Test
  void discardLeavesCurrentIllustrationsUnchanged() {
    commands.replaceIllustrations(
        key, List.of(new Illustration("https://images.example/original.png", 0)));
    var generation = run(0);
    generations.discard(key.value(), generation.id());
    assertThat(queries.get(key).illustrations())
        .containsExactly(new Illustration("https://images.example/original.png", 0));
  }
}
