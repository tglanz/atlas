package com.tglanz.atlas.backend.generation.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface GenerationRequestJpaRepository extends JpaRepository<GenerationRequestEntity, String> {
  List<GenerationRequestEntity> findByMovementKeyOrderByCreatedAtDesc(String movementKey);
}
