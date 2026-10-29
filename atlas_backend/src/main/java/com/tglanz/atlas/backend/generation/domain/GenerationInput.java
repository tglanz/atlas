package com.tglanz.atlas.backend.generation.domain;

import java.util.List;

/** The immutable input captured when a generation is requested. */
public record GenerationInput(
    String name,
    String description,
    List<String> cues,
    String guidelines,
    String avatarSpecification,
    String viewpointSpecification) {
  public GenerationInput {
    if (name == null || name.isBlank() || name.length() > 200) {
      throw new IllegalArgumentException("A movement name of at most 200 characters is required");
    }
    name = name.strip();
    description = description == null ? "" : description;
    guidelines = guidelines == null ? "" : guidelines;
    if (description.length() > 20000 || guidelines.length() > 10000) {
      throw new IllegalArgumentException("Description or guidelines are too long");
    }
    if (cues == null
        || cues.size() > 100
        || cues.stream().anyMatch(c -> c == null || c.length() > 2000)) {
      throw new IllegalArgumentException("Provide at most 100 cues, each at most 2000 characters");
    }
    cues = List.copyOf(cues);
    if (avatarSpecification == null || viewpointSpecification == null) {
      throw new IllegalArgumentException("Avatar and viewpoint specifications are required");
    }
  }
}
