package com.tglanz.atlas.backend.illustration.application.port.out;

import com.tglanz.atlas.backend.illustration.domain.Avatar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvatarRepository {
  List<Avatar> findAll();

  Optional<Avatar> findById(UUID id);

  Optional<Avatar> findDefault();

  /** Throws ConflictException when the name is taken. */
  void save(Avatar avatar);

  /** Throws ConflictException when the Avatar is still referenced by illustrations. */
  void deleteById(UUID id);
}
