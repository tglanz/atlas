package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.avatar.application.port.in.AvatarUseCase;
import com.tglanz.atlas.backend.generation.application.port.in.GenerationUseCase;
import com.tglanz.atlas.backend.generation.application.port.in.ViewpointUseCase;
import com.tglanz.atlas.backend.generation.domain.GenerationMovementIllustrations;
import com.tglanz.atlas.backend.generation.domain.Viewpoint;
import com.tglanz.atlas.backend.movement.domain.Movement;
import java.util.Comparator;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/** Composes illustration results independently of the workspace's editable generation draft. */
@Component
public class GenerationPanel {
  private final AvatarUseCase avatars;
  private final ViewpointUseCase viewpoints;
  private final GenerationUseCase generations;

  public GenerationPanel(
      AvatarUseCase avatars, ViewpointUseCase viewpoints, GenerationUseCase generations) {
    this.avatars = avatars;
    this.viewpoints = viewpoints;
    this.generations = generations;
  }

  public void addTo(Model model, Movement movement) {
    if (movement == null) {
      return;
    }
    var avatarList = avatars.list();
    var viewpointList = viewpoints.list();
    var runs = generations.list(movement.key().value());
    model.addAttribute("generationAvatars", avatarList);
    model.addAttribute("generationViewpoints", viewpointList);
    model.addAttribute("generationRunning", runs.stream().anyMatch(g -> g.isInProgress()));
    model.addAttribute(
        "latestGeneration",
        runs.stream()
            .max(Comparator.comparing(GenerationMovementIllustrations::createdAt))
            .orElse(null));
    if (!model.containsAttribute("generationAvatarId")) {
      model.addAttribute(
          "generationAvatarId",
          avatarList.size() == 1 ? avatarList.getFirst().id().toString() : "");
    }
    if (!model.containsAttribute("generationViewpointId")) {
      model.addAttribute(
          "generationViewpointId",
          viewpointList.stream()
              .filter(Viewpoint::isDefault)
              .findFirst()
              .or(
                  () ->
                      viewpointList.size() == 1
                          ? viewpointList.stream().findFirst()
                          : java.util.Optional.empty())
              .map(v -> v.id().toString())
              .orElse(""));
    }
  }
}
