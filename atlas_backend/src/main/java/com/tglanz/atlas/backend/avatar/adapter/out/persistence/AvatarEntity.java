package com.tglanz.atlas.backend.avatar.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "avatar")
class AvatarEntity {
  @Id String id;
  String name;
  String specification;
}
