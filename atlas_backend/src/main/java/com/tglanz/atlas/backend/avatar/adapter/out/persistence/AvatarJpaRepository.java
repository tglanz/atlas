package com.tglanz.atlas.backend.avatar.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface AvatarJpaRepository extends JpaRepository<AvatarEntity, String> {
  List<AvatarEntity> findAllByOrderByNameAsc();

  Optional<AvatarEntity> findFirstByIsDefaultTrue();
}
