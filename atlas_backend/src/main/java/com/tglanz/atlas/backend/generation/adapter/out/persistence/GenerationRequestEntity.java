package com.tglanz.atlas.backend.generation.adapter.out.persistence;

import com.tglanz.atlas.backend.generation.domain.GenerationStatus;
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
import java.util.List;

@Entity
@Table(name = "generation_request")
class GenerationRequestEntity {

  @Id String id;

  @Column(name = "movement_key")
  String movementKey;

  @Column(name = "draft_revision")
  int draftRevision;

  @Column(name = "avatar_id")
  String avatarId;

  @Column(name = "viewpoint_id")
  String viewpointId;

  @Enumerated(EnumType.STRING)
  GenerationStatus status;

  @Column(name = "failure_reason")
  String failureReason;

  @Column(name = "created_at")
  Instant createdAt;

  @Column(name = "completed_at")
  Instant completedAt;

  @ElementCollection
  @CollectionTable(name = "generation_stage", joinColumns = @JoinColumn(name = "request_id"))
  @OrderColumn(name = "position")
  List<StageRow> stages = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "generation_result", joinColumns = @JoinColumn(name = "request_id"))
  @OrderColumn(name = "position")
  @Column(name = "url")
  List<String> results = new ArrayList<>();

  @Embeddable
  static class StageRow {
    String description;

    @Column(name = "reference_url")
    String referenceUrl;
  }
}
