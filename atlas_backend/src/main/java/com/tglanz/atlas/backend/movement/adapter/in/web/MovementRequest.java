package com.tglanz.atlas.backend.movement.adapter.in.web;

import com.tglanz.atlas.backend.movement.domain.MovementContent;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import com.tglanz.atlas.backend.movement.domain.MovementLink;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

record MovementRequest(
    @NotBlank @Size(max = 200) String name,
    @Size(max = 20000) String description,
    List<@NotBlank @Size(max = 2000) String> cues,
    Set<@NotBlank @Size(max = 100) String> tags,
    List<@Valid LinkRequest> links) {

  record LinkRequest(@NotBlank String target, @NotBlank String relationType) {}

  MovementContent toContent() {
    List<MovementLink> mapped =
        links == null
            ? List.of()
            : links.stream()
                .map(l -> new MovementLink(new MovementKey(l.target()), l.relationType()))
                .toList();
    return new MovementContent(
        name, description, cues, tags == null ? new HashSet<>() : tags, mapped);
  }
}
