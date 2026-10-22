package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.domain.ItemType;
import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementItem;
import com.tglanz.atlas.backend.movement.domain.MovementStage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

record MovementRequest(
    @NotBlank @Size(max = 200) String name,
    @Size(max = 20000) String description,
    List<@Valid ItemRequest> items,
    List<@Valid StageRequest> stages,
    Set<@NotBlank @Size(max = 100) String> tags) {

  record ItemRequest(@NotNull ItemType type, @NotBlank @Size(max = 2000) String content) {}

  record StageRequest(
      @NotBlank @Size(max = 200) String name, @NotBlank @Size(max = 4000) String description) {}

  MovementContent toContent() {
    List<MovementItem> mappedItems =
        items == null
            ? List.of()
            : items.stream().map(i -> new MovementItem(i.type(), i.content())).toList();
    List<MovementStage> mappedStages =
        stages == null
            ? List.of()
            : stages.stream().map(s -> new MovementStage(s.name(), s.description())).toList();
    return new MovementContent(
        name, description, mappedItems, mappedStages, tags == null ? new HashSet<>() : tags);
  }
}
