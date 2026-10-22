package com.tglanz.atlas.backend.generation.domain;

import java.util.UUID;

/** A predefined way to specify the viewing angle of a generated Illustration. */
public record Viewpoint(UUID id, String name, String specification) {
  public Viewpoint {
    if (id == null) {
      throw new IllegalArgumentException("Id is required");
    }
    if (name == null || name.isBlank() || name.length() > 100) {
      throw new IllegalArgumentException("Viewpoint name is required (max 100 characters)");
    }
    if (specification == null || specification.isBlank()) {
      throw new IllegalArgumentException("Viewpoint specification is required");
    }
    name = name.strip();
  }

  public static Viewpoint create(String name, String specification) {
    return new Viewpoint(UUID.randomUUID(), name, specification);
  }

  public Viewpoint edit(String newName, String newSpecification) {
    return new Viewpoint(id, newName, newSpecification);
  }
}
