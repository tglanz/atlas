package com.tglanz.atlas.backend.generation.application.port.out;

/** A provider request failed, for good. */
public class ImageGenerationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ImageGenerationException(String message) {
    super(message);
  }

  public ImageGenerationException(String message, Throwable cause) {
    super(message, cause);
  }
}
