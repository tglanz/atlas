package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.out.ImageRequest;
import java.util.List;

/**
 * What a run has to do, with everything resolved so no database access is needed to carry it out.
 */
public record GenerationPlan(List<Step> steps) {

  public sealed interface Step permits Existing, Generate {}

  /** A static image: the picture is used as is. */
  public record Existing(String url) implements Step {}

  /** A picture to generate. */
  public record Generate(ImageRequest request) implements Step {}
}
