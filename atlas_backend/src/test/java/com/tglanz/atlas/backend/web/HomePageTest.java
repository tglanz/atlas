package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tglanz.atlas.backend.graph.application.port.in.GraphUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementSummary;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

class HomePageTest {
  private final MovementQueryUseCase movements = mock(MovementQueryUseCase.class);
  private final MovementPages pages =
      new MovementPages(
          movements,
          mock(GraphUseCase.class),
          mock(MovementPageService.class),
          new PageMetadataFactory("https://atlas.example"),
          mock(GenerationPanel.class),
          mock(PreviewImages.class));

  @Test
  void featuresThreeDistinctIllustratedMovementsAndKeepsTheFullLibraryCount() {
    var illustrated =
        List.of(
            summary("a", "/a.png"),
            summary("b", "/b.png"),
            summary("c", "/c.png"),
            summary("d", "/d.png"));
    when(movements.search("", Set.of()))
        .thenReturn(
            List.of(
                summary("first", null),
                illustrated.get(0),
                illustrated.get(1),
                illustrated.get(2),
                illustrated.get(3)));
    var model = new ExtendedModelMap();
    assertThat(pages.home(model)).isEqualTo("home");
    assertThat((List<?>) model.get("featuredMovements"))
        .hasSize(3)
        .doesNotHaveDuplicates()
        .allMatch(illustrated::contains);
    assertThat(model.get("movementCount")).isEqualTo(5);
  }

  @Test
  void doesNotFillEmptySpotsWithMovementsThatHaveNoIllustration() {
    var first = summary("a", "/a.png");
    var second = summary("b", "/b.png");
    when(movements.search("", Set.of()))
        .thenReturn(List.of(summary("missing", null), first, second));
    var model = new ExtendedModelMap();
    pages.home(model);
    assertThat((List<?>) model.get("featuredMovements"))
        .hasSize(2)
        .allMatch(item -> item.equals(first) || item.equals(second))
        .doesNotHaveDuplicates();
  }

  @Test
  void anUnillustratedLibraryHasNoFeaturedMovements() {
    when(movements.search("", Set.of())).thenReturn(List.of(summary("a", null)));
    var model = new ExtendedModelMap();
    pages.home(model);
    assertThat((List<?>) model.get("featuredMovements")).isEmpty();
  }

  private static MovementSummary summary(String key, String image) {
    return new MovementSummary(new MovementKey(key), key, List.of(), image);
  }
}
