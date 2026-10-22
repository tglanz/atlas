package com.tglanz.atlas.backend.generation.domain;

import java.util.List;

/** The stages of a Movement's Illustration sequence. One Illustration results per stage. */
public record IllustrationsGenerationSpec(List<StageSpec> stages) {
  private static final int MAX_STAGES = 20;

  public IllustrationsGenerationSpec {
    if (stages == null || stages.isEmpty() || stages.size() > MAX_STAGES) {
      throw new IllegalArgumentException(
          "Between 1 and %d stages are required".formatted(MAX_STAGES));
    }
    stages = List.copyOf(stages);
  }
}
