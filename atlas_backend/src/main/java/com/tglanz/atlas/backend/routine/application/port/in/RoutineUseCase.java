package com.tglanz.atlas.backend.routine.application.port.in;

import com.tglanz.atlas.backend.routine.domain.Routine;
import com.tglanz.atlas.backend.routine.domain.RoutineContent;
import java.util.List;
import java.util.UUID;

/** Owner operations remain private; shared reads require an active public-link token. */
public interface RoutineUseCase {
  List<Routine> list(String ownerId);

  Routine get(String ownerId, UUID id);

  Routine create(String ownerId, RoutineContent content);

  Routine update(String ownerId, UUID id, RoutineContent content);

  Routine getShared(UUID token);

  UUID enableSharing(String ownerId, UUID id);

  void disableSharing(String ownerId, UUID id);

  void delete(String ownerId, UUID id);
}
