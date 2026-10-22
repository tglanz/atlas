package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.in.MediaUploadUseCase;
import com.tglanz.atlas.backend.generation.application.port.in.Picture;
import com.tglanz.atlas.backend.generation.application.port.out.PictureStorage;
import org.springframework.stereotype.Service;

@Service
public class MediaUploadService implements MediaUploadUseCase {

  private final PictureStorage storage;

  public MediaUploadService(PictureStorage storage) {
    this.storage = storage;
  }

  @Override
  public String upload(Picture picture) {
    PictureRules.validate(picture);
    return storage.store(picture.contentType(), picture.content());
  }
}
