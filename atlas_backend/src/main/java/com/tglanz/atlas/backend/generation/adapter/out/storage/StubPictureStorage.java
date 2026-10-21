package com.tglanz.atlas.backend.generation.adapter.out.storage;

import com.tglanz.atlas.backend.generation.application.port.out.PictureStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Stand-in for S3: stores nothing and returns a fixed placeholder url. */
@Component
class StubPictureStorage implements PictureStorage {

  private final String url;

  StubPictureStorage(@Value("${atlas.storage.stub-url}") String url) {
    this.url = url;
  }

  @Override
  public String store(String contentType, byte[] content) {
    return url;
  }
}
