package com.tglanz.atlas.backend.generation.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface ViewpointJpaRepository extends JpaRepository<ViewpointEntity, String> {
  List<ViewpointEntity> findAllByOrderByNameAsc();

  Optional<ViewpointEntity> findFirstByIsDefaultTrue();
}
