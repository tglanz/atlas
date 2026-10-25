package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.graph.domain.Edge;
import com.tglanz.atlas.backend.movement.domain.ItemType;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementItem;
import com.tglanz.atlas.backend.movement.domain.MovementStage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Mutable HTML form values, kept separate from the domain model. */
public class MovementForm {
  @NotBlank
  @Size(max = 100)
  private String key = "";

  @NotBlank
  @Size(max = 200)
  private String name = "";

  @Size(max = 20000)
  private String description = "";

  private List<@Size(max = 2000) String> cues = new ArrayList<>();
  private List<@Size(max = 2000) String> comments = new ArrayList<>();
  private List<@Size(max = 100) String> tags = new ArrayList<>();
  @Valid private List<Stage> stages = new ArrayList<>();
  @Valid private List<Relationship> links = new ArrayList<>();

  public static MovementForm from(Movement movement, List<Edge> outgoing) {
    var form = new MovementForm();
    form.key = movement.key().value();
    form.name = movement.content().name();
    form.description = movement.content().description();
    movement
        .content()
        .items()
        .forEach(
            item -> {
              if (item.type() == ItemType.CUE) form.cues.add(item.content());
              else if (item.type() == ItemType.COMMENT) form.comments.add(item.content());
            });
    form.tags.addAll(movement.content().tags().stream().sorted().toList());
    movement
        .content()
        .stages()
        .forEach(stage -> form.stages.add(new Stage(stage.name(), stage.description())));
    outgoing.forEach(edge -> form.links.add(new Relationship(edge.target(), edge.type())));
    return form;
  }

  MovementContent toContent() {
    List<MovementItem> items = new ArrayList<>();
    cues.stream()
        .filter(value -> value != null && !value.isBlank())
        .forEach(value -> items.add(new MovementItem(ItemType.CUE, value)));
    comments.stream()
        .filter(value -> value != null && !value.isBlank())
        .forEach(value -> items.add(new MovementItem(ItemType.COMMENT, value)));
    return new MovementContent(
        name,
        description,
        items,
        stages.stream().map(stage -> new MovementStage(stage.name, stage.description)).toList(),
        new LinkedHashSet<>(
            tags.stream().filter(value -> value != null && !value.isBlank()).toList()));
  }

  List<Edge> toEdges(String movementKey) {
    return links.stream().map(link -> new Edge(movementKey, link.target, link.type)).toList();
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getCues() {
    return cues;
  }

  public void setCues(List<String> cues) {
    this.cues = cues;
  }

  public List<String> getComments() {
    return comments;
  }

  public void setComments(List<String> comments) {
    this.comments = comments;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public List<Stage> getStages() {
    return stages;
  }

  public void setStages(List<Stage> stages) {
    this.stages = stages;
  }

  public List<Relationship> getLinks() {
    return links;
  }

  public void setLinks(List<Relationship> links) {
    this.links = links;
  }

  public static class Stage {
    @NotBlank
    @Size(max = 200)
    private String name = "";

    @NotBlank
    @Size(max = 4000)
    private String description = "";

    public Stage() {}

    public Stage(String name, String description) {
      this.name = name;
      this.description = description;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }

  public static class Relationship {
    @NotBlank
    @Size(max = 100)
    private String target = "";

    @NotBlank
    @Size(max = 64)
    private String type = "";

    public Relationship() {}

    public Relationship(String target, String type) {
      this.target = target;
      this.type = type;
    }

    public String getTarget() {
      return target;
    }

    public void setTarget(String target) {
      this.target = target;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }
}
