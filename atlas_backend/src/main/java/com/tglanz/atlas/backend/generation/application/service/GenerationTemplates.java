package com.tglanz.atlas.backend.generation.application.service;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import com.tglanz.atlas.backend.generation.application.port.out.ImageRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Renders the system prompt and the image prompt of one provider request from the templates in
 * `resources/generation`. The templates only reference the model built here.
 */
@Component
public class GenerationTemplates {

  /** What the templates can reference. Absent texts are empty, so their sections are skipped. */
  public record Model(
      String movementName,
      String movementDescription,
      List<String> movementCues,
      String avatarSpecification,
      String viewpointSpecification,
      String stageName,
      String stageDescription) {}

  private final Template systemPrompt = load("system-prompt");
  private final Template imagePrompt = load("image-prompt");

  public ImageRequest render(Model model) {
    Map<String, Object> context =
        Map.of(
            "movement",
            Map.of(
                "name", model.movementName(),
                "description", nullToEmpty(model.movementDescription()),
                "cues", model.movementCues()),
            "avatar",
            Map.of("specification", model.avatarSpecification()),
            "viewpoint",
            Map.of("specification", model.viewpointSpecification()),
            "stage",
            Map.of("name", model.stageName(), "description", model.stageDescription()));
    return new ImageRequest(
        systemPrompt.execute(context).strip(), imagePrompt.execute(context).strip());
  }

  private static Template load(String name) {
    String path = "generation/" + name + ".mustache";
    try (InputStream in = GenerationTemplates.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException("Missing template " + path);
      }
      Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
      // The prompts are plain text, so nothing is HTML-escaped; empty texts count as absent.
      return Mustache.compiler().escapeHTML(false).emptyStringIsFalse(true).compile(reader);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String nullToEmpty(String text) {
    return text == null || text.isBlank() ? "" : text;
  }
}
