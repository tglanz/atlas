package com.tglanz.atlas.backend.routine.domain;

import java.util.UUID;

/** A practitioner's Routine. Its owner controls editing and optional public link sharing. */
public final class Routine {
  private final UUID id;
  private final String ownerId;
  private RoutineContent content;
  private final UUID shareToken;

  private Routine(UUID id, String ownerId, RoutineContent content, UUID shareToken) {
    if (id == null || ownerId == null || ownerId.isBlank() || content == null) {
      throw new IllegalArgumentException("Id, owner and content are required");
    }
    this.id = id;
    this.ownerId = ownerId;
    this.content = content;
    this.shareToken = shareToken;
  }

  public static Routine create(String ownerId, RoutineContent content) {
    return new Routine(UUID.randomUUID(), ownerId, content, null);
  }

  public static Routine restore(UUID id, String ownerId, RoutineContent content) {
    return restore(id, ownerId, content, null);
  }

  public static Routine restore(UUID id, String ownerId, RoutineContent content, UUID shareToken) {
    return new Routine(id, ownerId, content, shareToken);
  }

  public UUID shareToken() {
    return shareToken;
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
