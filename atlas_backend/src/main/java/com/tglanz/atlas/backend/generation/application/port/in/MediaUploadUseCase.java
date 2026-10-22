package com.tglanz.atlas.backend.generation.application.port.in;

public interface MediaUploadUseCase {

  /** Stores a picture and returns its url, for use in a generation spec. */
  String upload(Picture picture);
}
