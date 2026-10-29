package com.tglanz.atlas.backend.movement.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.ItemType;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementInstructions;
import com.tglanz.atlas.backend.movement.domain.MovementItem;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MovementInstructionsTest {
  @Autowired MovementCommandUseCase commands;
  @Autowired MovementQueryUseCase queries;
  @Autowired EntityManagerFactory entityManagerFactory;

  @Test
  void instructionsReadCurrentNamesOrderedIllustrationsAndOnlyCuesInThreeStatements() {
    MovementKey detailed = new MovementKey("instructions_z");
    MovementKey empty = new MovementKey("instructions_a");
    commands.create(
        detailed, new MovementContent("Original name", "", List.of(), List.of(), Set.of()));
    commands.update(
        detailed,
        new MovementContent(
            "Instructions detailed",
            "",
            List.of(
                new MovementItem(ItemType.CUE, "First cue"),
                new MovementItem(ItemType.COMMENT, "A comment is not a cue"),
                new MovementItem(ItemType.CUE, "Second cue")),
            List.of(),
            Set.of("test")));
    commands.replaceIllustrations(
        detailed, List.of(new Illustration("first-image"), new Illustration("second-image")));
    commands.create(
        empty, new MovementContent("Instructions empty", "", List.of(), List.of(), Set.of()));
    var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    statistics.clear();

    List<MovementInstructions> result =
        queries.instructions(
            List.of(detailed, new MovementKey("instructions_missing"), empty, detailed));

    assertThat(result).extracting(MovementInstructions::key).containsExactly(detailed, empty);
    assertThat(result.getFirst().name()).isEqualTo("Instructions detailed");
    assertThat(result.getFirst().illustrations())
        .extracting(Illustration::imageUrl)
        .containsExactly("first-image", "second-image");
    assertThat(result.getFirst().cues()).containsExactly("First cue", "Second cue");
    assertThat(result.getLast().illustrations()).isEmpty();
    assertThat(result.getLast().cues()).isEmpty();
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
  }

  @Test
  void emptyInstructionsRequireNoStatements() {
    var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    statistics.clear();

    assertThat(queries.instructions(List.of())).isEmpty();
    assertThat(statistics.getPrepareStatementCount()).isZero();
  }

  @Test
  void unknownMovementKeysAreIgnored() {
    assertThat(queries.instructions(List.of(new MovementKey("instructions_unknown")))).isEmpty();
  }
}
