package com.tglanz.atlas.backend.generation.application.port.in;

import java.util.List;

/** Edits used only for this request; the saved movement is never updated. */
public record GenerationDraft(
    String name, String description, List<String> cues, List<Stage> stages, String guidelines) {
  public GenerationDraft {
    if (stages == null || stages.isEmpty() || stages.size() > 20) {
      throw new IllegalArgumentException("Provide between 1 and 20 stages");
    }
    stages = List.copyOf(stages);
  }

  public record Stage(String name, String description) {
    public Stage {
      if (name == null
          || name.isBlank()
          || name.length() > 200
          || description == null
          || description.isBlank()
          || description.length() > 4000) {
        throw new IllegalArgumentException("Each stage needs a name and a description");
      }
    }
  }
}
