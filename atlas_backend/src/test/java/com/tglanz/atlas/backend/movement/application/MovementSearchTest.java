package com.tglanz.atlas.backend.movement.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.Illustration;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementSummary;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MovementSearchTest {

  @Autowired MovementCommandUseCase commands;
  @Autowired MovementQueryUseCase queries;
  @Autowired EntityManagerFactory entityManagerFactory;

  private static final String PREFIX = "srch";

  private void create(String key, String name, Set<String> tags, List<String> images) {
    commands.create(
        new MovementKey(key), new MovementContent(name, "desc", List.of(), List.of(), tags));
    commands.replaceIllustrations(
        new MovementKey(key), images.stream().map(Illustration::new).toList());
  }

  private void seed() {
    if (queries.exists(new MovementKey(PREFIX + "_a"))) {
      return;
    }
    create(PREFIX + "_a", "Srch alpha", Set.of("push", "chest"), List.of("img-a-0", "img-a-1"));
    create(PREFIX + "_b", "Srch beta", Set.of("push"), List.of());
    create(PREFIX + "_c", "Srch gamma", Set.of(), List.of("img-c-0"));
  }

  private List<MovementSummary> search(String text, Set<String> tags) {
    return queries.search(text, tags);
  }

  @Test
  void summariesHaveSortedTagsAndTheFirstIllustration() {
    seed();

    var summaries = search("srch", Set.of());

    assertThat(summaries)
        .extracting(s -> s.key().value())
        .containsExactly("srch_a", "srch_b", "srch_c");
    assertThat(summaries.get(0).tags()).containsExactly("chest", "push");
    assertThat(summaries.get(0).representativeIllustration()).isEqualTo("img-a-0");
    assertThat(summaries.get(1).representativeIllustration()).isNull();
    assertThat(summaries.get(2).tags()).isEmpty();
    assertThat(summaries.get(2).representativeIllustration()).isEqualTo("img-c-0");
  }

  @Test
  void filtersByTextAndRequiresEveryTagButReturnsAllTheTags() {
    seed();

    assertThat(search("BETA", Set.of())).extracting(s -> s.key().value()).containsExactly("srch_b");
    var both = search("srch", Set.of("push", "chest"));
    assertThat(both).extracting(s -> s.key().value()).containsExactly("srch_a");
    assertThat(both.get(0).tags()).containsExactly("chest", "push");
    assertThat(search("srch", Set.of("push"))).hasSize(2);
  }

  @Test
  void aSearchIsOneStatementWhateverTheNumberOfMovements() {
    seed();
    var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    statistics.clear();

    search("", Set.of());

    assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
  }
}
