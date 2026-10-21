package com.tglanz.atlas.backend.generation.application.port.in;

import java.util.UUID;

public interface IllustrationUploadUseCase {

  /** Stores the picture as is and appends it to the Avatar's sequence on the open draft. */
  String upload(String movementKey, UUID avatarId, String contentType, byte[] content);
}
