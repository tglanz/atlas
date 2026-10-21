package com.tglanz.atlas.backend.avatar.domain;

import java.util.UUID;

/** A kind of figure users can choose to see. Exactly one Avatar is the default. */
public record Avatar(UUID id, String name, String specification, boolean isDefault) {
  public Avatar {
    if (id == null) {
      throw new IllegalArgumentException("Id is required");
    }
    if (name == null || name.isBlank() || name.length() > 100) {
      throw new IllegalArgumentException("Avatar name is required (max 100 characters)");
    }
    if (specification == null || specification.isBlank()) {
      throw new IllegalArgumentException("Avatar specification is required");
    }
    name = name.strip();
  }

  public static Avatar create(String name, String specification, boolean isDefault) {
    return new Avatar(UUID.randomUUID(), name, specification, isDefault);
  }

  public Avatar edit(String newName, String newSpecification) {
    return new Avatar(id, newName, newSpecification, isDefault);
  }

  public Avatar withDefault(boolean value) {
    return new Avatar(id, name, specification, value);
  }
}
