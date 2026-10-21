package com.tglanz.atlas.backend.illustration.application.port.in;

import com.tglanz.atlas.backend.illustration.domain.Viewpoint;
import java.util.List;
import java.util.UUID;

public interface ViewpointUseCase {
  List<Viewpoint> list();

  Viewpoint get(UUID id);

  Viewpoint create(String name, String specification);

  Viewpoint update(UUID id, String name, String specification);

  void delete(UUID id);
}
