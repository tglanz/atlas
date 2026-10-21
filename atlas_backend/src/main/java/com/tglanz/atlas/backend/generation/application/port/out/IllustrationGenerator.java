package com.tglanz.atlas.backend.generation.application.port.out;

import java.util.List;

public interface IllustrationGenerator {

  /** One picture per stage, in stage order. */
  List<GeneratedPicture> generate(GenerationInput input);
}
