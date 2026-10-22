package com.tglanz.atlas.backend.avatar.application.port.in;

import com.tglanz.atlas.backend.avatar.domain.Avatar;
import java.util.List;
import java.util.UUID;

public interface AvatarUseCase {
  List<Avatar> list();

  Avatar get(UUID id);

  Avatar create(String name, String specification);

  Avatar update(UUID id, String name, String specification);

  void delete(UUID id);
}
