package com.tglanz.atlas.backend.movement.domain;

import com.tglanz.atlas.backend.shared.error.ConflictException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** One revision of a Movement. Drafts are editable; published revisions are immutable. */
public final class Movement {
  private final MovementKey key;
  private final int revision;
  private RevisionStatus status;
  private Instant publishedAt;
  private MovementContent content;
  private final Map<UUID, List<Illustration>> illustrations = new LinkedHashMap<>();

  private Movement(
      MovementKey key,
      int revision,
      RevisionStatus status,
      Instant publishedAt,
      MovementContent content,
      Map<UUID, List<Illustration>> illustrations) {
    if (key == null || content == null) {
      throw new IllegalArgumentException("Key and content are required");
    }
    if (revision < 1) {
      throw new IllegalArgumentException("Revision starts at 1");
    }
    if ((status == RevisionStatus.PUBLISHED) != (publishedAt != null)) {
      throw new IllegalArgumentException("Only published revisions have a publication time");
    }
    this.key = key;
    this.revision = revision;
    this.status = status;
    this.publishedAt = publishedAt;
    this.content = content;
    illustrations.forEach((avatarId, list) -> this.illustrations.put(avatarId, List.copyOf(list)));
  }

  /** A new Movement starts as a draft at revision 1. */
  public static Movement newDraft(MovementKey key, MovementContent content) {
    return new Movement(key, 1, RevisionStatus.DRAFT, null, content, Map.of());
  }

  /** Rebuilds a stored revision. */
  public static Movement restore(
      MovementKey key,
      int revision,
      RevisionStatus status,
      Instant publishedAt,
      MovementContent content,
      Map<UUID, List<Illustration>> illustrations) {
    return new Movement(key, revision, status, publishedAt, content, illustrations);
  }

  public void edit(MovementContent newContent) {
    requireDraft("Only drafts can be edited");
    if (newContent == null) {
      throw new IllegalArgumentException("Content is required");
    }
    this.content = newContent;
  }

  public void publish(Instant at) {
    requireDraft("Only drafts can be published");
    this.status = RevisionStatus.PUBLISHED;
    this.publishedAt = at;
  }

  /** Creates the next revision as a draft, carrying over this revision's content. */
  public Movement revise() {
    if (status != RevisionStatus.PUBLISHED) {
      throw new ConflictException("Only published revisions can be revised");
    }
    return new Movement(key, revision + 1, RevisionStatus.DRAFT, null, content, illustrations());
  }

  /** Replaces the Avatar's whole sequence; an empty list removes it. */
  public void replaceIllustrations(UUID avatarId, List<Illustration> sequence) {
    requireDraft("Only drafts can change illustrations");
    if (avatarId == null || sequence == null) {
      throw new IllegalArgumentException("Avatar and illustrations are required");
    }
    if (sequence.isEmpty()) {
      illustrations.remove(avatarId);
    } else {
      illustrations.put(avatarId, List.copyOf(sequence));
    }
  }

  /** Appends to the end of the Avatar's sequence. */
  public void addIllustration(UUID avatarId, Illustration illustration) {
    requireDraft("Only drafts can change illustrations");
    if (avatarId == null || illustration == null) {
      throw new IllegalArgumentException("Avatar and illustration are required");
    }
    List<Illustration> sequence = new ArrayList<>(illustrations.getOrDefault(avatarId, List.of()));
    sequence.add(illustration);
    illustrations.put(avatarId, List.copyOf(sequence));
  }

  private void requireDraft(String message) {
    if (status != RevisionStatus.DRAFT) {
      throw new ConflictException(message);
    }
  }

  public MovementKey key() {
    return key;
  }

  public int revision() {
    return revision;
  }

  public RevisionStatus status() {
    return status;
  }

  public Instant publishedAt() {
    return publishedAt;
  }

  public MovementContent content() {
    return content;
  }

  public Map<UUID, List<Illustration>> illustrations() {
    return Collections.unmodifiableMap(new LinkedHashMap<>(illustrations));
  }
}
