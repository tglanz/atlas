package com.tglanz.atlas.backend.generation.adapter.in.web;

import com.tglanz.atlas.backend.generation.application.port.in.IllustrationUploadUseCase;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/movements/{key}/draft/avatars/{avatarId}/illustrations")
class IllustrationUploadController {

  record UploadResponse(String url) {}

  private final IllustrationUploadUseCase useCase;

  IllustrationUploadController(IllustrationUploadUseCase useCase) {
    this.useCase = useCase;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<UploadResponse> upload(
      @PathVariable String key,
      @PathVariable UUID avatarId,
      @RequestPart("file") MultipartFile file)
      throws IOException {
    String url = useCase.upload(key, avatarId, file.getContentType(), file.getBytes());
    return ResponseEntity.created(URI.create("/api/movements/" + key + "/draft"))
        .body(new UploadResponse(url));
  }
}
