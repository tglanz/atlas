package com.tglanz.atlas.backend.generation.application.service;

import com.tglanz.atlas.backend.generation.application.port.in.IllustrationUploadUseCase;
import com.tglanz.atlas.backend.generation.application.port.out.MovementDrafts;
import com.tglanz.atlas.backend.generation.application.port.out.PictureStorage;
import com.tglanz.atlas.backend.shared.error.NotFoundException;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IllustrationUploadService implements IllustrationUploadUseCase {

  static final Set<String> CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/webp");
  static final int MAX_BYTES = 10 * 1024 * 1024;

  private final PictureStorage storage;
  private final MovementDrafts drafts;

  public IllustrationUploadService(PictureStorage storage, MovementDrafts drafts) {
    this.storage = storage;
    this.drafts = drafts;
  }

  @Override
  public String upload(String movementKey, UUID avatarId, String contentType, byte[] content) {
    if (contentType == null || !CONTENT_TYPES.contains(contentType)) {
      throw new IllegalArgumentException("Only PNG, JPEG and WebP pictures are accepted");
    }
    if (content == null || content.length == 0 || content.length > MAX_BYTES) {
      throw new IllegalArgumentException("Picture must be between 1 byte and 10 MB");
    }
    if (drafts.findDraft(movementKey).isEmpty()) {
      throw new NotFoundException("No draft for movement " + movementKey);
    }
    String url = storage.store(contentType, content);
    drafts.addIllustration(movementKey, avatarId, url);
    return url;
  }
}
