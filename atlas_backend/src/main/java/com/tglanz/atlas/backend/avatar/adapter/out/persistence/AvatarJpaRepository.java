package com.tglanz.atlas.backend.avatar.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface AvatarJpaRepository extends JpaRepository<AvatarEntity, String> {
  List<AvatarEntity> findAllByOrderByNameAsc();
}
