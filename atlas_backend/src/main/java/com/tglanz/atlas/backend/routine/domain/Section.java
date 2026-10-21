package com.tglanz.atlas.backend.routine.domain;

import java.util.List;

public record Section(String name, String description, List<MovementRef> movements) {
  public Section {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Section name is required");
    }
    name = name.strip();
    description = description == null ? "" : description;
    movements = movements == null ? List.of() : List.copyOf(movements);
  }
}
