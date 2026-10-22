package com.tglanz.atlas.backend.generation.application.port.out;

import com.tglanz.atlas.backend.generation.domain.Viewpoint;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViewpointRepository {
  List<Viewpoint> findAll();

  Optional<Viewpoint> findById(UUID id);

  Optional<Viewpoint> findDefault();

  /** Throws ConflictException when the name is taken. */
  void save(Viewpoint viewpoint);

  /** Throws ConflictException when the Viewpoint is still referenced. */
  void deleteById(UUID id);
}
