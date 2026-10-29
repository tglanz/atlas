package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.MovementInstructions;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementName;
import com.tglanz.atlas.backend.routine.application.port.in.RoutineUseCase;
import com.tglanz.atlas.backend.routine.domain.Routine;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/routines")
public class RoutinePages {
  public record RoutineSummary(UUID id, String name, String description) {}

  public record SectionView(
      String name, String description, List<MovementInstructions> movements) {}

  public record RoutineView(UUID id, String name, String description, List<SectionView> sections) {}

  private final RoutineUseCase routines;
  private final MovementQueryUseCase movements;
  private final PageMetadataFactory metadata;

  public RoutinePages(
      RoutineUseCase routines, MovementQueryUseCase movements, PageMetadataFactory metadata) {
    this.routines = routines;
    this.movements = movements;
    this.metadata = metadata;
  }

  @InitBinder("routineForm")
  void bindRoutine(WebDataBinder binder) {
    binder.setAllowedFields(
        "name",
        "description",
        "sections[*].name",
        "sections[*].description",
        "sections[*].movements[*]");
  }

  @GetMapping
  String list(Principal principal, Model model) {
    model.addAttribute("meta", metadata.privatePage("Routines"));
    model.addAttribute(
        "routines",
        routines.list(principal.getName()).stream()
            .map(r -> new RoutineSummary(r.id(), r.content().name(), r.content().description()))
            .toList());
    return "routines/index";
  }

  @GetMapping("/new")
  String create(@RequestParam(required = false) String movement, Model model) {
    var form = new RoutineForm();
    if (movement != null) {
      var selected = movements.get(new MovementKey(movement));
      var section = new RoutineForm.SectionForm();
      section.setName("Practice");
      section.setMovements(List.of(selected.key().value()));
      form.setSections(List.of(section));
    }
    model.addAttribute("routineForm", form);
    return editor(model, null);
  }

  @GetMapping("/add")
  String chooseRoutine(Principal principal, @RequestParam String movement, Model model) {
    var selected = movements.get(new MovementKey(movement));
    model.addAttribute("meta", metadata.privatePage("Add to a routine"));
    model.addAttribute("movement", selected);
    model.addAttribute("routines", routines.list(principal.getName()));
    return "routines/add";
  }

  @PostMapping("/{id}/add-movement")
  String addMovement(
      Principal principal,
      @PathVariable UUID id,
      @RequestParam String movement,
      @RequestParam int section) {
    routines.addMovement(principal.getName(), id, section, movement);
    return "redirect:/routines/" + id;
  }

  @GetMapping("/{id}")
  String detail(
      Principal principal,
      @PathVariable UUID id,
      @RequestParam(defaultValue = "false") boolean sharing,
      Model model) {
    Routine routine = routines.get(principal.getName(), id);
    model.addAttribute("sharingOpen", sharing);
    model.addAttribute(
        "shareUrl",
        routine.shareToken() == null
            ? null
            : metadata.absolute("/routines/shared/" + routine.shareToken()));
    return view(routine, false, model);
  }

  @GetMapping("/shared/{token}")
  String shared(@PathVariable String token, Model model, HttpServletResponse response) {
    response.setHeader("Referrer-Policy", "no-referrer");
    UUID parsed;
    try {
      parsed = UUID.fromString(token);
    } catch (IllegalArgumentException exception) {
      throw new NotFoundException("This shared routine is unavailable");
    }
    return view(routines.getShared(parsed), true, model);
  }

  @PostMapping("/{id}/share")
  String share(Principal principal, @PathVariable UUID id) {
    routines.enableSharing(principal.getName(), id);
    return "redirect:/routines/" + id + "?sharing=true#routine-sharing";
  }

  @PostMapping("/{id}/unshare")
  String unshare(Principal principal, @PathVariable UUID id) {
    routines.disableSharing(principal.getName(), id);
    return "redirect:/routines/" + id + "?sharing=true#routine-sharing";
  }

  private String view(Routine routine, boolean publicView, Model model) {
    model.addAttribute("publicView", publicView);
    List<MovementKey> keys =
        new LinkedHashSet<>(routine.content().movementKeys())
            .stream().map(MovementKey::new).toList();
    Map<String, MovementInstructions> instructions =
        movements.instructions(keys).stream()
            .collect(Collectors.toMap(m -> m.key().value(), m -> m));
    model.addAttribute("meta", metadata.privatePage(routine.content().name()));
    model.addAttribute(
        "routine",
        new RoutineView(
            routine.id(),
            routine.content().name(),
            routine.content().description(),
            routine.content().sections().stream()
                .map(
                    section ->
                        new SectionView(
                            section.name(),
                            section.description(),
                            section.movements().stream()
                                .map(
                                    key ->
                                        instructions.getOrDefault(
                                            key,
                                            new MovementInstructions(
                                                new MovementKey(key), key, List.of(), List.of())))
                                .toList()))
                .toList()));
    return "routines/detail";
  }

  @GetMapping("/{id}/edit")
  String edit(Principal principal, @PathVariable UUID id, Model model) {
    Routine routine = routines.get(principal.getName(), id);
    model.addAttribute("routineForm", RoutineForm.from(routine.content()));
    return editor(model, id);
  }

  @PostMapping("/new")
  String create(
      Principal principal,
      @Valid @ModelAttribute("routineForm") RoutineForm form,
      BindingResult errors,
      Model model,
      HttpServletResponse response) {
    if (!errors.hasErrors()) {
      try {
        Routine saved = routines.create(principal.getName(), form.toContent());
        return "redirect:/routines/" + saved.id();
      } catch (IllegalArgumentException e) {
        errors.reject("routine.invalid", e.getMessage());
      }
    }
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    return editor(model, null);
  }

  @PostMapping("/{id}/edit")
  String update(
      Principal principal,
      @PathVariable UUID id,
      @Valid @ModelAttribute("routineForm") RoutineForm form,
      BindingResult errors,
      Model model,
      HttpServletResponse response) {
    // Ownership applies even when validation fails before any command is issued.
    routines.get(principal.getName(), id);
    if (!errors.hasErrors()) {
      try {
        routines.update(principal.getName(), id, form.toContent());
        return "redirect:/routines/" + id;
      } catch (IllegalArgumentException e) {
        errors.reject("routine.invalid", e.getMessage());
      }
    }
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    return editor(model, id);
  }

  @PostMapping("/{id}/delete")
  String delete(Principal principal, @PathVariable UUID id) {
    routines.delete(principal.getName(), id);
    return "redirect:/routines";
  }

  private String editor(Model model, UUID id) {
    model.addAttribute("pageScripts", List.of("routines"));
    List<MovementName> choices = movements.names();
    model.addAttribute("meta", metadata.privatePage(id == null ? "New routine" : "Edit routine"));
    model.addAttribute("routineId", id);
    model.addAttribute("movementChoices", choices);
    model.addAttribute(
        "movementNames",
        choices.stream().collect(Collectors.toMap(m -> m.key().value(), MovementName::name)));
    return "routines/editor";
  }
}
