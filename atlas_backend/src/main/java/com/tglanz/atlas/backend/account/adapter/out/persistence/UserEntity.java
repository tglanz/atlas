package com.tglanz.atlas.backend.account.adapter.out.persistence;

import com.tglanz.atlas.backend.account.domain.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "app_user")
class UserEntity {
  @Id String id;
  String email;

  @Column(name = "password_hash")
  String passwordHash;

  @Enumerated(EnumType.STRING)
  Role role;

  @Column(name = "created_at")
  Instant createdAt;
}
