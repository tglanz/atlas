package com.tglanz.atlas.backend.avatar.application.port.in;

import com.tglanz.atlas.backend.avatar.domain.Avatar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvatarUseCase {
  List<Avatar> list();

  Avatar get(UUID id);

  Optional<Avatar> findDefault();

  /** The first Avatar created becomes the default. */
  Avatar create(String name, String specification);

  Avatar update(UUID id, String name, String specification);

  /** Makes the Avatar the default, replacing the previous default. */
  Avatar makeDefault(UUID id);

  /** The default Avatar cannot be deleted. */
  void delete(UUID id);
}
