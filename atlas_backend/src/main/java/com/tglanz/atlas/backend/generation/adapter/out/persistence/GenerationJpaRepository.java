package com.tglanz.atlas.backend.generation.adapter.out.persistence;

import com.tglanz.atlas.backend.generation.domain.GenerationStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface GenerationJpaRepository extends JpaRepository<GenerationEntity, String> {
  List<GenerationEntity> findByMovementKeyOrderByCreatedAtDesc(String movementKey);

  List<GenerationEntity> findByStatusIn(Collection<GenerationStatus> statuses);

  boolean existsByMovementKeyAndStatusIn(String movementKey, Collection<GenerationStatus> statuses);
}
