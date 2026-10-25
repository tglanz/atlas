package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.avatar.application.port.in.AvatarUseCase;
import com.tglanz.atlas.backend.generation.application.port.in.GenerationUseCase;
import com.tglanz.atlas.backend.generation.application.port.in.ViewpointUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IllustrationPages {
  public record FailedSpecification(String target, String name, String specification) {}

  private final AvatarUseCase avatars;
  private final ViewpointUseCase viewpoints;
  private final GenerationUseCase generations;
  private final MovementQueryUseCase movements;
  private final MovementCommandUseCase movementCommands;
  private final GenerationPanel panel;
  private final PageMetadataFactory metadata;

  public IllustrationPages(
      AvatarUseCase avatars,
      ViewpointUseCase viewpoints,
      GenerationUseCase generations,
      MovementQueryUseCase movements,
      MovementCommandUseCase movementCommands,
      GenerationPanel panel,
      PageMetadataFactory metadata) {
    this.avatars = avatars;
    this.viewpoints = viewpoints;
    this.generations = generations;
    this.movements = movements;
    this.movementCommands = movementCommands;
    this.panel = panel;
    this.metadata = metadata;
  }

  @GetMapping("/settings/illustrations")
  String settings(@RequestParam(defaultValue = "avatars") String tab, Model model) {
    model.addAttribute("meta", metadata.privatePage("Illustrations"));
    model.addAttribute("settingsTab", "viewpoints".equals(tab) ? "viewpoints" : "avatars");
    model.addAttribute("avatars", avatars.list());
    model.addAttribute("viewpoints", viewpoints.list());
    return "settings/illustrations";
  }

  @PostMapping("/settings/illustrations/avatars")
  String createAvatar(
      @RequestParam String name,
      @RequestParam String specification,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    return saveSpecification(
        "avatars",
        "new",
        name,
        specification,
        () -> avatars.create(name, specification),
        model,
        response,
        redirect);
  }

  @PostMapping("/settings/illustrations/avatars/{id}")
  String updateAvatar(
      @PathVariable UUID id,
      @RequestParam String name,
      @RequestParam String specification,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    return saveSpecification(
        "avatars",
        id.toString(),
        name,
        specification,
        () -> avatars.update(id, name, specification),
        model,
        response,
        redirect);
  }

  @PostMapping("/settings/illustrations/avatars/{id}/delete")
  String deleteAvatar(
      @PathVariable UUID id,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    return changeSettings("avatars", () -> avatars.delete(id), model, response, redirect);
  }

  @PostMapping("/settings/illustrations/viewpoints")
  String createViewpoint(
      @RequestParam String name,
      @RequestParam String specification,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    return saveSpecification(
        "viewpoints",
        "new",
        name,
        specification,
        () -> viewpoints.create(name, specification),
        model,
        response,
        redirect);
  }

  @PostMapping("/settings/illustrations/viewpoints/{id}")
  String updateViewpoint(
      @PathVariable UUID id,
      @RequestParam String name,
      @RequestParam String specification,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    return saveSpecification(
        "viewpoints",
        id.toString(),
        name,
        specification,
        () -> viewpoints.update(id, name, specification),
        model,
        response,
        redirect);
  }

  @PostMapping("/settings/illustrations/viewpoints/{id}/delete")
  String deleteViewpoint(
      @PathVariable UUID id,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    return changeSettings("viewpoints", () -> viewpoints.delete(id), model, response, redirect);
  }

  @PostMapping("/settings/illustrations/viewpoints/{id}/default")
  String makeDefault(
      @PathVariable UUID id,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    return changeSettings(
        "viewpoints", () -> viewpoints.makeDefault(id), model, response, redirect);
  }

  @GetMapping("/movements/{key}/generation-status")
  String generationStatus(@PathVariable String key, Model model, HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-store");
    var movement = movements.get(new MovementKey(key));
    model.addAttribute("movement", movement);
    panel.addTo(model, movement);
    return "fragments/generation :: panel";
  }

  @PostMapping("/movements/{key}/generations")
  String generate(
      @PathVariable String key,
      @RequestParam UUID avatarId,
      @RequestParam UUID viewpointId,
      @RequestHeader(name = "X-Fragment", defaultValue = "false") boolean fragment,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    model.addAttribute("generationAvatarId", avatarId.toString());
    model.addAttribute("generationViewpointId", viewpointId.toString());
    if (!fragment) {
      redirect.addFlashAttribute("generationAvatarId", avatarId.toString());
      redirect.addFlashAttribute("generationViewpointId", viewpointId.toString());
    }
    return changeGeneration(
        key,
        fragment,
        () -> generations.request(key, avatarId, viewpointId),
        "Generation requested.",
        model,
        response,
        redirect);
  }

  @PostMapping("/movements/{key}/generations/{id}/apply")
  String apply(
      @PathVariable String key,
      @PathVariable UUID id,
      @RequestHeader(name = "X-Fragment", defaultValue = "false") boolean fragment,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    return changeGeneration(
        key,
        fragment,
        () -> generations.apply(key, id),
        "Illustrations applied.",
        model,
        response,
        redirect);
  }

  @PostMapping("/movements/{key}/generations/{id}/discard")
  String discard(
      @PathVariable String key,
      @PathVariable UUID id,
      @RequestHeader(name = "X-Fragment", defaultValue = "false") boolean fragment,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    return changeGeneration(
        key,
        fragment,
        () -> generations.discard(key, id),
        "Generation discarded.",
        model,
        response,
        redirect);
  }

  @PostMapping("/movements/{key}/illustrations/clear")
  String clear(
      @PathVariable String key,
      @RequestHeader(name = "X-Fragment", defaultValue = "false") boolean fragment,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    return changeGeneration(
        key,
        fragment,
        () -> movementCommands.replaceIllustrations(new MovementKey(key), List.of()),
        "Illustrations removed.",
        model,
        response,
        redirect);
  }

  private String saveSpecification(
      String tab,
      String target,
      String name,
      String specification,
      Runnable change,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    model.addAttribute("failedSpecification", new FailedSpecification(target, name, specification));
    return changeSettings(tab, change, model, response, redirect);
  }

  private String changeSettings(
      String tab,
      Runnable change,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    try {
      change.run();
      redirect.addFlashAttribute("notice", "Illustration settings saved.");
      return "redirect:/settings/illustrations?tab=" + tab;
    } catch (IllegalArgumentException | ConflictException error) {
      response.setStatus(error instanceof ConflictException ? 409 : 400);
      model.addAttribute("settingsError", error.getMessage());
      return settings(tab, model);
    }
  }

  private String changeGeneration(
      String key,
      boolean fragment,
      Runnable change,
      String notice,
      Model model,
      HttpServletResponse response,
      RedirectAttributes redirect) {
    try {
      change.run();
      if (!fragment) {
        redirect.addFlashAttribute("generationNotice", notice);
        return "redirect:/movements/" + key + "/edit#generation-panel";
      }
      model.addAttribute("generationNotice", notice);
    } catch (IllegalArgumentException | ConflictException error) {
      if (!fragment) {
        redirect.addFlashAttribute("generationError", error.getMessage());
        return "redirect:/movements/" + key + "/edit#generation-panel";
      }
      response.setStatus(error instanceof ConflictException ? 409 : 400);
      model.addAttribute("generationError", error.getMessage());
    }
    return generationStatus(key, model, response);
  }
}
