package com.tglanz.atlas.backend.generation.adapter.out.generator;

import com.tglanz.atlas.backend.generation.application.port.out.GeneratedPicture;
import com.tglanz.atlas.backend.generation.application.port.out.IllustrationGenerator;
import com.tglanz.atlas.backend.generation.application.port.out.ImageRequest;
import org.springframework.stereotype.Component;

/** Stand-in for the third-party generator: returns an empty picture. */
@Component
class StubIllustrationGenerator implements IllustrationGenerator {

  @Override
  public GeneratedPicture generate(ImageRequest request) {
    return new GeneratedPicture(new byte[0], "image/png");
  }
}
