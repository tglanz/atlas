package com.tglanz.atlas.backend.routine.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routine")
class RoutineEntity {

  @Id String id;

  @Column(name = "owner_id")
  String ownerId;

  String name;
  String description;

  @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("position")
  List<SectionEntity> sections = new ArrayList<>();

  @Entity
  @Table(name = "routine_section")
  static class SectionEntity {
    @Id String id;

    @ManyToOne
    @JoinColumn(name = "routine_id")
    RoutineEntity routine;

    int position;
    String name;
    String description;

    @ElementCollection
    @CollectionTable(
        name = "routine_section_movement",
        joinColumns = @JoinColumn(name = "section_id"))
    @OrderColumn(name = "position")
    List<MovementRefRow> movements = new ArrayList<>();
  }

  @Embeddable
  static class MovementRefRow {
    @Column(name = "movement_key")
    String movementKey;

    @Column(name = "movement_revision")
    int movementRevision;
  }
}
