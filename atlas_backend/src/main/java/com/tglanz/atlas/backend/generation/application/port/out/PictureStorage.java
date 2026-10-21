package com.tglanz.atlas.backend.generation.application.port.out;

public interface PictureStorage {

  /** Stores a picture and returns the url it can be viewed at. */
  String store(String contentType, byte[] content);
}
