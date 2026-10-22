package com.tglanz.atlas.backend.routine.domain;

import java.util.List;

public record RoutineContent(String name, String description, List<Section> sections) {
  public RoutineContent {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Routine name is required");
    }
    name = name.strip();
    description = description == null ? "" : description;
    sections = sections == null ? List.of() : List.copyOf(sections);
  }

  public List<String> movementKeys() {
    return sections.stream().flatMap(s -> s.movements().stream()).toList();
  }
}
