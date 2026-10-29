package com.tglanz.atlas.backend.routine.application.port.out;

import com.tglanz.atlas.backend.routine.domain.Routine;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineRepository {
  List<Routine> findByOwner(String ownerId);

  Optional<Routine> findById(UUID id);

  Optional<Routine> findByShareToken(UUID token);

  /** Enable once, preserving an existing link; return empty if the Routine was removed. */
  Optional<UUID> enableSharing(UUID id);

  void disableSharing(UUID id);

  void save(Routine routine);

  void deleteById(UUID id);
}
