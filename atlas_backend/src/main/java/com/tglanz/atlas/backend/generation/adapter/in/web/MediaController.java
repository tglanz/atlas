package com.tglanz.atlas.backend.generation.adapter.in.web;

import com.tglanz.atlas.backend.generation.application.port.in.MediaUploadUseCase;
import com.tglanz.atlas.backend.generation.application.port.in.Picture;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
class MediaController {

  record UploadResponse(String url) {}

  private final MediaUploadUseCase useCase;

  MediaController(MediaUploadUseCase useCase) {
    this.useCase = useCase;
  }

  /** Stores a picture and returns its url, to use as an image in a generation spec. */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  UploadResponse upload(@RequestPart("file") MultipartFile file) throws IOException {
    return new UploadResponse(useCase.upload(new Picture(file.getContentType(), file.getBytes())));
  }
}
