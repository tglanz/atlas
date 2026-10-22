package com.tglanz.atlas.backend.routine.domain;

import java.util.List;

/** A named group of Movements, referenced by key, in order. */
public record Section(String name, String description, List<String> movements) {
  public Section {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Section name is required");
    }
    name = name.strip();
    description = description == null ? "" : description;
    movements = movements == null ? List.of() : List.copyOf(movements);
    if (movements.stream().anyMatch(key -> key == null || key.isBlank())) {
      throw new IllegalArgumentException("Movement keys must not be blank");
    }
  }
}
