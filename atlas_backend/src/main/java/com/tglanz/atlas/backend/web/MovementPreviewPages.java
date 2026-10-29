package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class MovementPreviewPages {
  private static final Logger log = LoggerFactory.getLogger(MovementPreviewPages.class);
  private final MovementQueryUseCase movements;
  private final PreviewImages previews;

  MovementPreviewPages(MovementQueryUseCase movements, PreviewImages previews) {
    this.movements = movements;
    this.previews = previews;
  }

  @GetMapping("/movements/{key}/preview/{version}.jpg")
  ResponseEntity<byte[]> image(@PathVariable String key, @PathVariable String version) {
    var movement = movements.get(new MovementKey(key));
    if (movement.illustrations().isEmpty()) return ResponseEntity.notFound().build();
    String source = movement.illustrations().getFirst().imageUrl();
    if (previews.path(key, source) == null || !PreviewImages.version(source).equals(version))
      return ResponseEntity.notFound().build();
    try {
      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_JPEG)
          .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
          .eTag("\"" + version + "\"")
          .body(previews.jpeg(source));
    } catch (IOException e) {
      log.warn("Could not create sharing image for {}: {}", key, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .cacheControl(CacheControl.noStore())
          .build();
    }
  }
}
