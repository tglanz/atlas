package com.tglanz.atlas.backend.account.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<UserEntity, String> {
  Optional<UserEntity> findByEmail(String email);
}
