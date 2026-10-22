package com.tglanz.atlas.backend.generation.application.port.out;

public interface IllustrationGenerator {

  /** Generates one picture. */
  GeneratedPicture generate(ImageRequest request);
}
