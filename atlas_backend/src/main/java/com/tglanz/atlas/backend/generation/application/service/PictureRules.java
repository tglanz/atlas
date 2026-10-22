package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.in.Picture;
import java.util.Set;

/** What pictures users may send. */
final class PictureRules {
  private static final Set<String> CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/webp");
  private static final int MAX_BYTES = 10 * 1024 * 1024;

  private PictureRules() {}

  static void validate(Picture picture) {
    if (picture.contentType() == null || !CONTENT_TYPES.contains(picture.contentType())) {
      throw new IllegalArgumentException("Only PNG, JPEG and WebP pictures are accepted");
    }
    if (picture.content() == null
        || picture.content().length == 0
        || picture.content().length > MAX_BYTES) {
      throw new IllegalArgumentException("Picture must be between 1 byte and 10 MB");
    }
  }
}
