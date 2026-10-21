package com.tglanz.atlas.backend.routine.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface RoutineJpaRepository extends JpaRepository<RoutineEntity, String> {
  List<RoutineEntity> findByOwnerIdOrderByNameAsc(String ownerId);
}
