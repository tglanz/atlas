package com.tglanz.atlas.backend.routine.application.port.in;

import com.tglanz.atlas.backend.routine.domain.Routine;
import com.tglanz.atlas.backend.routine.domain.RoutineContent;
import java.util.List;
import java.util.UUID;

/** All operations act on the given owner's Routines only. */
public interface RoutineUseCase {
  List<Routine> list(String ownerId);

  Routine get(String ownerId, UUID id);

  Routine create(String ownerId, RoutineContent content);

  Routine update(String ownerId, UUID id, RoutineContent content);

  void delete(String ownerId, UUID id);
}
