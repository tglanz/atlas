package com.tglanz.atlas.backend.routine.domain;

import java.util.UUID;

/** A practitioner's Routine. Only its owner may see or change it. */
public final class Routine {
  private final UUID id;
  private final String ownerId;
  private RoutineContent content;

  private Routine(UUID id, String ownerId, RoutineContent content) {
    if (id == null || ownerId == null || ownerId.isBlank() || content == null) {
      throw new IllegalArgumentException("Id, owner and content are required");
    }
    this.id = id;
    this.ownerId = ownerId;
    this.content = content;
  }

  public static Routine create(String ownerId, RoutineContent content) {
    return new Routine(UUID.randomUUID(), ownerId, content);
  }

  public static Routine restore(UUID id, String ownerId, RoutineContent content) {
    return new Routine(id, ownerId, content);
  }

  public void edit(RoutineContent newContent) {
    if (newContent == null) {
      throw new IllegalArgumentException("Content is required");
    }
    this.content = newContent;
  }

  public boolean isOwnedBy(String userId) {
    return ownerId.equals(userId);
  }

  public UUID id() {
    return id;
  }

  public String ownerId() {
    return ownerId;
  }

  public RoutineContent content() {
    return content;
  }
}
