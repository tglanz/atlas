package com.tglanz.atlas.backend.generation.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface ViewpointJpaRepository extends JpaRepository<ViewpointEntity, String> {
  List<ViewpointEntity> findAllByOrderByNameAsc();
}
