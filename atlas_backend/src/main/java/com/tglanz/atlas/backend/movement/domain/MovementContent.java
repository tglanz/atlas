package com.tglanz.atlas.backend.movement.domain;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** The editable content of a Movement revision. */
public record MovementContent(
    String name,
    String description,
    List<String> cues,
    Set<String> tags,
    List<MovementLink> links) {

  public MovementContent {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Movement name is required");
    }
    name = name.strip();
    description = description == null ? "" : description;
    cues = cues == null ? List.of() : List.copyOf(cues);
    if (cues.stream().anyMatch(c -> c == null || c.isBlank())) {
      throw new IllegalArgumentException("Cues must not be blank");
    }
    tags =
        tags == null
            ? Set.of()
            : tags.stream()
                .map(t -> t == null ? "" : t.strip().toLowerCase())
                .peek(
                    t -> {
                      if (t.isEmpty()) {
                        throw new IllegalArgumentException("Tags must not be blank");
                      }
                    })
                .collect(Collectors.toUnmodifiableSet());
    links = links == null ? List.of() : List.copyOf(links);
  }
}
