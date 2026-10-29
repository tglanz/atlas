package com.tglanz.atlas.backend.generation.adapter.out.persistence;

import com.tglanz.atlas.backend.generation.domain.CandidateState;
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
@Table(name = "generation_movement_illustrations_request")
class GenerationEntity {

  @Id String id;

  @Column(name = "movement_key")
  String movementKey;

  @Column(name = "input_name")
  String inputName;

  @Column(name = "input_description")
  String inputDescription;

  String guidelines;

  @Column(name = "avatar_specification")
  String avatarSpecification;

  @Column(name = "viewpoint_specification")
  String viewpointSpecification;

  @Column(name = "stage_fingerprint")
  String stageFingerprint;

  @Column(name = "target_stage_position")
  Integer targetStagePosition;

  @ElementCollection
  @CollectionTable(name = "generation_input_cue", joinColumns = @JoinColumn(name = "request_id"))
  @OrderColumn(name = "position")
  @Column(name = "content")
  List<String> inputCues = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  GenerationStatus status;

  @Column(name = "failure_reason")
  String failureReason;

  @Column(name = "created_at")
  Instant createdAt;

  @Column(name = "finished_at")
  Instant finishedAt;

  @ElementCollection
  @CollectionTable(
      name = "generation_illustration_stage",
      joinColumns = @JoinColumn(name = "request_id"))
  @OrderColumn(name = "position")
  List<StageRow> stages = new ArrayList<>();

  @Embeddable
  static class StageRow {
    @Column(name = "avatar_id")
    String avatarId;

    @Column(name = "viewpoint_id")
    String viewpointId;

    String name;

    String description;

    @Column(name = "candidate_state")
    @Enumerated(EnumType.STRING)
    CandidateState candidateState;

    @Column(name = "candidate_image_url")
    String candidateImageUrl;

    @Column(name = "candidate_failure_reason")
    String candidateFailureReason;

    @Column(name = "candidate_progress")
    String candidateProgress;

    String provider;
    String model;

    @Column(name = "provider_request_id")
    String providerRequestId;

    @Column(name = "provider_status_url")
    String providerStatusUrl;

    @Column(name = "provider_result_url")
    String providerResultUrl;

    @Column(name = "obtained_at")
    Instant obtainedAt;
  }
}
