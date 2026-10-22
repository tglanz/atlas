package com.tglanz.atlas.backend.movement.adapter.out.persistence;

import com.tglanz.atlas.backend.movement.domain.ItemType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "movement")
class MovementEntity {

  @Id
  @Column(name = "movement_key")
  String movementKey;

  String name;
  String description;

  @ElementCollection
  @CollectionTable(name = "movement_item", joinColumns = @JoinColumn(name = "movement_key"))
  @OrderColumn(name = "position")
  List<ItemRow> items = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "movement_tag", joinColumns = @JoinColumn(name = "movement_key"))
  @Column(name = "tag")
  Set<String> tags = new HashSet<>();

  @ElementCollection
  @CollectionTable(name = "movement_link", joinColumns = @JoinColumn(name = "movement_key"))
  @OrderColumn(name = "position")
  List<LinkRow> links = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "movement_illustration", joinColumns = @JoinColumn(name = "movement_key"))
  @OrderColumn(name = "position")
  List<IllustrationRow> illustrations = new ArrayList<>();

  @Embeddable
  static class ItemRow {
    @Enumerated(EnumType.STRING)
    ItemType type;

    String content;
  }

  @Embeddable
  static class LinkRow {
    @Column(name = "target_key")
    String targetKey;

    @Column(name = "relation_type")
    String relationType;
  }

  @Embeddable
  static class IllustrationRow {
    @Column(name = "url")
    String url;
  }
}
