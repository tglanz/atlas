package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.graph.application.port.in.GraphUseCase;
import com.tglanz.atlas.backend.graph.domain.EdgeType;
import com.tglanz.atlas.backend.graph.domain.RelatedMovement;
import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.ItemType;
import com.tglanz.atlas.backend.movement.domain.Movement;
import com.tglanz.atlas.backend.movement.domain.MovementItem;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.shared.error.ConflictException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class MovementPages {
  private static final String LIBRARY_DESCRIPTION =
      "Explore movements, illustrations, practical cues and connected progressions. Search by name and filter by tags.";

  private final MovementQueryUseCase movements;
  private final GraphUseCase graph;
  private final MovementPageService saves;
  private final PageMetadataFactory metadata;
  private final GenerationPanel generation;
  private final PreviewImages previews;

  public MovementPages(
      MovementQueryUseCase movements,
      GraphUseCase graph,
      MovementPageService saves,
      PageMetadataFactory metadata,
      GenerationPanel generation,
      PreviewImages previews) {
    this.movements = movements;
    this.graph = graph;
    this.saves = saves;
    this.metadata = metadata;
    this.generation = generation;
    this.previews = previews;
  }

  @InitBinder("form")
  void bindForm(WebDataBinder binder) {
    binder.setAllowedFields(
        "key",
        "name",
        "description",
        "cues[*]",
        "comments[*]",
        "tags[*]",
        "stages[*].name",
        "stages[*].description",
        "links[*].target",
        "links[*].type");
  }

  @GetMapping("/")
  String home(Model model) {
    var library = movements.search("", Set.of());
    model.addAttribute(
        "featuredMovements",
        library.stream()
            .sorted(Comparator.comparing(movement -> movement.representativeIllustration() == null))
            .limit(3)
            .toList());
    model.addAttribute("movementCount", library.size());
    model.addAttribute(
        "meta", metadata.publicPage("Movement library", LIBRARY_DESCRIPTION, "/", null));
    return "home";
  }

  @GetMapping("/movements")
  String index(
      @RequestParam(defaultValue = "") String text,
      @RequestParam(required = false) List<String> tags,
      @RequestHeader(name = "X-Atlas-Fragment", required = false) String fragment,
      Model model,
      HttpServletResponse response) {
    model.addAttribute("pageScripts", List.of("movements"));
    String search = text.strip();
    List<String> selected =
        tags == null
            ? List.of()
            : tags.stream()
                .map(tag -> tag.strip().toLowerCase(Locale.ROOT))
                .filter(tag -> !tag.isBlank())
                .distinct()
                .sorted()
                .toList();
    var path = UriComponentsBuilder.fromPath("/movements");
    if (!search.isEmpty()) path.queryParam("text", search);
    selected.forEach(tag -> path.queryParam("tags", tag));
    var meta =
        metadata.publicPage(
            "Movement index", LIBRARY_DESCRIPTION, path.build().encode().toUriString(), null);
    if (!search.isEmpty() || !selected.isEmpty()) meta = meta.withRobots("noindex,follow");
    model.addAttribute("meta", meta);
    model.addAttribute("text", search);
    model.addAttribute("selectedTags", selected);
    var filters = new ArrayList<TagFilter>();
    for (String tag : selected) {
      var withoutTag = UriComponentsBuilder.fromPath("/movements");
      if (!search.isEmpty()) withoutTag.queryParam("text", search);
      selected.stream()
          .filter(other -> !other.equals(tag))
          .forEach(other -> withoutTag.queryParam("tags", other));
      filters.add(new TagFilter(tag, withoutTag.build().encode().toUriString()));
    }
    model.addAttribute("selectedFilters", filters);
    model.addAttribute("movements", movements.search(search, new LinkedHashSet<>(selected)));
    response.addHeader("Vary", "X-Atlas-Fragment");
    if ("movements".equals(fragment)) return "movements/index :: results";
    var allTags = movements.tags();
    model.addAttribute("allTags", allTags);
    model.addAttribute("knownTags", allTags.stream().map(tag -> tag.tag()).toList());
    model.addAttribute("movementNames", movements.names());
    return "movements/index";
  }

  @GetMapping("/movements/{key}")
  String details(@PathVariable String key, Model model) {
    Movement movement = movements.get(new MovementKey(key));
    var view = graph.edgesWithMovements(key);
    Map<String, RelatedMovement> byKey =
        view.targets().stream().collect(Collectors.toMap(RelatedMovement::key, target -> target));
    var groups = new LinkedHashMap<String, List<RelatedMovement>>();
    view.edges()
        .outgoing()
        .forEach(
            edge ->
                groups
                    .computeIfAbsent(edge.type(), ignored -> new ArrayList<>())
                    .add(
                        byKey.getOrDefault(
                            edge.target(),
                            new RelatedMovement(edge.target(), edge.target(), List.of(), null))));
    List<EdgeType> types = graph.edgeTypes();
    List<RelatedGroup> related =
        groups.entrySet().stream()
            .sorted(Comparator.comparingInt(entry -> typeOrder(types, entry.getKey())))
            .map(
                entry ->
                    new RelatedGroup(
                        types.stream()
                            .filter(type -> type.name().equals(entry.getKey()))
                            .map(EdgeType::plural)
                            .findFirst()
                            .orElse(capitalize(entry.getKey())),
                        entry.getValue()))
            .toList();
    List<ItemGroup> items =
        List.of(
            new ItemGroup(
                "Cues",
                true,
                movement.content().items().stream()
                    .filter(item -> item.type() == ItemType.CUE)
                    .toList()),
            new ItemGroup(
                "Comments",
                false,
                movement.content().items().stream()
                    .filter(item -> item.type() == ItemType.COMMENT)
                    .toList()));
    String image =
        movement.illustrations().isEmpty() ? null : movement.illustrations().getFirst().imageUrl();
    String preview = previews.path(key, image);
    PageMetadata page =
        metadata.publicPage(
            movement.content().name(),
            movement.content().description().isBlank()
                ? LIBRARY_DESCRIPTION
                : movement.content().description(),
            "/movements/" + key,
            preview == null ? image : preview);
    if (preview != null)
      page = page.withImageDetails("image/jpeg", PreviewImages.SIZE, PreviewImages.SIZE);
    model.addAttribute("meta", page);
    model.addAttribute("movement", movement);
    model.addAttribute("itemGroups", items);
    model.addAttribute("relatedGroups", related);
    return "movements/details";
  }

  @GetMapping("/movements/new")
  String create(Model model) {
    model.addAttribute("form", new MovementForm());
    return editor(model, null);
  }

  @GetMapping("/movements/{key}/edit")
  String edit(@PathVariable String key, Model model) {
    Movement movement = movements.get(new MovementKey(key));
    model.addAttribute("form", MovementForm.from(movement, graph.edgesOf(key).outgoing()));
    return editor(model, movement);
  }

  @PostMapping("/movements/new")
  String create(
      @Valid @ModelAttribute("form") MovementForm form,
      BindingResult errors,
      Model model,
      HttpServletResponse response) {
    return save(form, errors, model, response, null);
  }

  @PostMapping("/movements/{key}/edit")
  String edit(
      @PathVariable String key,
      @Valid @ModelAttribute("form") MovementForm form,
      BindingResult errors,
      Model model,
      HttpServletResponse response) {
    form.setKey(key);
    return save(form, errors, model, response, movements.get(new MovementKey(key)));
  }

  private String save(
      MovementForm form,
      BindingResult errors,
      Model model,
      HttpServletResponse response,
      Movement existing) {
    if (existing == null && "new".equals(form.getKey())) {
      errors.rejectValue(
          "key",
          "movement.key.reserved",
          "Choose a different key; new is reserved for movement creation.");
    }
    if (!errors.hasErrors()) {
      try {
        saves.save(form, existing == null);
        return "redirect:/movements/" + form.getKey();
      } catch (IllegalArgumentException | ConflictException exception) {
        errors.reject("movement.save", exception.getMessage());
      }
    }
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    return editor(model, existing);
  }

  private String editor(Model model, Movement movement) {
    model.addAttribute("pageScripts", List.of("movements", "generation"));
    model.addAttribute(
        "meta",
        metadata.privatePage(
            movement == null ? "New movement" : "Edit " + movement.content().name()));
    model.addAttribute("movement", movement);
    model.addAttribute("movementNames", movements.names());
    var types = new LinkedHashMap<String, String>();
    graph.edgeTypes().forEach(type -> types.put(type.name(), type.label()));
    MovementForm form = (MovementForm) model.getAttribute("form");
    form.getLinks().stream()
        .map(MovementForm.Relationship::getType)
        .filter(Objects::nonNull)
        .filter(type -> !type.isBlank())
        .forEach(type -> types.putIfAbsent(type, capitalize(type)));
    model.addAttribute("relationshipTypes", types);
    generation.addTo(model, movement);
    return "movements/editor";
  }

  private static int typeOrder(List<EdgeType> types, String name) {
    for (int index = 0; index < types.size(); index++)
      if (types.get(index).name().equals(name)) return index;
    return types.size();
  }

  private static String capitalize(String value) {
    return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
  }

  public record RelatedGroup(String title, List<RelatedMovement> movements) {}

  public record TagFilter(String name, String url) {}

  public record ItemGroup(String title, boolean ordered, List<MovementItem> items) {}
}
