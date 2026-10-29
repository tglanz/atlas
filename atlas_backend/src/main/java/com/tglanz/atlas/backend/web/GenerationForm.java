package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.generation.application.port.in.GenerationDraft;
import com.tglanz.atlas.backend.movement.domain.ItemType;
import com.tglanz.atlas.backend.movement.domain.Movement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Temporary generation inputs, never saved to the movement. */
public class GenerationForm {
  @NotNull private UUID avatarId = null;
  @NotNull private UUID viewpointId = null;

  @NotBlank
  @Size(max = 200)
  private String name = "";

  @Size(max = 20000)
  private String description = "";

  @Size(max = 200000)
  private String cues = "";

  @Size(max = 10000)
  private String guidelines = "";

  @Valid
  @Size(min = 1, max = 20)
  private List<MovementForm.Stage> stages = new ArrayList<>();

  @Min(0)
  private Integer stagePosition = null;

  public static GenerationForm from(Movement movement) {
    var form = new GenerationForm();
    form.name = movement.content().name();
    form.description = movement.content().description();
    form.cues =
        String.join(
            "\n",
            movement.content().items().stream()
                .filter(i -> i.type() == ItemType.CUE)
                .map(i -> i.content())
                .toList());
    movement
        .content()
        .stages()
        .forEach(s -> form.stages.add(new MovementForm.Stage(s.name(), s.description())));
    return form;
  }

  GenerationDraft toDraft() {
    return new GenerationDraft(
        name,
        description,
        cues == null
            ? List.of()
            : cues.lines().map(String::strip).filter(s -> !s.isEmpty()).toList(),
        stages.stream()
            .map(s -> new GenerationDraft.Stage(s.getName(), s.getDescription()))
            .toList(),
        guidelines);
  }

  public UUID getAvatarId() {
    return avatarId;
  }

  public void setAvatarId(UUID value) {
    avatarId = value;
  }

  public UUID getViewpointId() {
    return viewpointId;
  }

  public void setViewpointId(UUID value) {
    viewpointId = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    name = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    description = value;
  }

  public String getCues() {
    return cues;
  }

  public void setCues(String value) {
    cues = value;
  }

  public String getGuidelines() {
    return guidelines;
  }

  public void setGuidelines(String value) {
    guidelines = value;
  }

  public List<MovementForm.Stage> getStages() {
    return stages;
  }

  public void setStages(List<MovementForm.Stage> value) {
    stages = value;
  }

  public Integer getStagePosition() {
    return stagePosition;
  }

  public void setStagePosition(Integer value) {
    stagePosition = value;
  }
}
