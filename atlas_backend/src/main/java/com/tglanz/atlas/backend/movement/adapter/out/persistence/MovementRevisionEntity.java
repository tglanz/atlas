package com.tglanz.atlas.backend.movement.adapter.out.persistence;

import com.tglanz.atlas.backend.movement.domain.RevisionStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "movement_revision")
class MovementRevisionEntity {

  @Id String id;

  @Column(name = "movement_key")
  String movementKey;

  int revision;

  @Enumerated(EnumType.STRING)
  RevisionStatus status;

  @Column(name = "published_at")
  Instant publishedAt;

  String name;
  String description;

  @ElementCollection
  @CollectionTable(name = "movement_cue", joinColumns = @JoinColumn(name = "revision_id"))
  @OrderColumn(name = "position")
  @Column(name = "cue")
  List<String> cues = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "movement_tag", joinColumns = @JoinColumn(name = "revision_id"))
  @Column(name = "tag")
  Set<String> tags = new HashSet<>();

  @ElementCollection
  @CollectionTable(name = "movement_link", joinColumns = @JoinColumn(name = "revision_id"))
  @OrderColumn(name = "position")
  List<LinkRow> links = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "movement_illustration", joinColumns = @JoinColumn(name = "revision_id"))
  @OrderColumn(name = "position")
  List<IllustrationRow> illustrations = new ArrayList<>();

  @Embeddable
  static class LinkRow {
    @Column(name = "target_key")
    String targetKey;

    @Column(name = "relation_type")
    String relationType;
  }

  @Embeddable
  static class IllustrationRow {
    @Column(name = "avatar_id")
    String avatarId;

    @Column(name = "url")
    String url;
  }
}
