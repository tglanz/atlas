package com.tglanz.atlas.backend.illustration.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "avatar")
class AvatarEntity {
  @Id String id;
  String name;
  String specification;

  @Column(name = "is_default")
  boolean isDefault;
}
