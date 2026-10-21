package com.tglanz.atlas.backend.generation.adapter.out.generator;

import com.tglanz.atlas.backend.generation.application.port.out.GeneratedPicture;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationInput;
import com.tglanz.atlas.backend.generation.application.port.out.IllustrationGenerator;
import java.util.List;
import org.springframework.stereotype.Component;

/** Stand-in for the third-party generator: returns one empty picture per stage. */
@Component
class StubIllustrationGenerator implements IllustrationGenerator {

  @Override
  public List<GeneratedPicture> generate(GenerationInput input) {
    return input.stages().stream()
        .map(s -> new GeneratedPicture(new byte[0], "image/png"))
        .toList();
  }
}
