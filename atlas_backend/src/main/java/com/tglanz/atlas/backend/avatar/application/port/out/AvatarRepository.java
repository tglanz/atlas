package com.tglanz.atlas.backend.avatar.application.port.out;

import com.tglanz.atlas.backend.avatar.domain.Avatar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvatarRepository {
  List<Avatar> findAll();

  Optional<Avatar> findById(UUID id);

  /** Throws ConflictException when the name is taken. */
  void save(Avatar avatar);

  /** Throws ConflictException when the Avatar is still referenced. */
  void deleteById(UUID id);
}
