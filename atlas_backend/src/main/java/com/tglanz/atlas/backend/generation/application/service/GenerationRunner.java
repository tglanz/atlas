package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.in.RunGenerationUseCase;
import com.tglanz.atlas.backend.generation.application.port.out.GeneratedPicture;
import com.tglanz.atlas.backend.generation.application.port.out.GenerationInput;
import com.tglanz.atlas.backend.generation.application.port.out.IllustrationGenerator;
import com.tglanz.atlas.backend.generation.application.port.out.PictureStorage;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GenerationRunner implements RunGenerationUseCase {

  private final GenerationLifecycle lifecycle;
  private final IllustrationGenerator generator;
  private final PictureStorage storage;

  public GenerationRunner(
      GenerationLifecycle lifecycle, IllustrationGenerator generator, PictureStorage storage) {
    this.lifecycle = lifecycle;
    this.generator = generator;
    this.storage = storage;
  }

  @Override
  public void run(UUID requestId) {
    GenerationInput input = lifecycle.begin(requestId).orElse(null);
    if (input == null) {
      return;
    }
    try {
      List<GeneratedPicture> pictures = generator.generate(input);
      List<String> urls =
          pictures.stream().map(p -> storage.store(p.contentType(), p.content())).toList();
      lifecycle.complete(requestId, urls);
    } catch (RuntimeException e) {
      lifecycle.fail(requestId, describe(e));
    }
  }

  private static String describe(RuntimeException e) {
    String message = e.getMessage();
    return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
  }
}
