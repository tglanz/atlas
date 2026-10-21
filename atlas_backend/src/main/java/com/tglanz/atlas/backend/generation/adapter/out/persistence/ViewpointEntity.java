package com.tglanz.atlas.backend.generation.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "viewpoint")
class ViewpointEntity {
  @Id String id;
  String name;
  String specification;
}
