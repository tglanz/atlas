package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.graph.application.port.in.GraphUseCase;
import com.tglanz.atlas.backend.movement.application.port.in.MovementCommandUseCase;
import com.tglanz.atlas.backend.movement.domain.MovementKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Keeps a page's content and relationship changes atomic. */
@Service
public class MovementPageService {
  private final MovementCommandUseCase commands;
  private final GraphUseCase graph;

  public MovementPageService(MovementCommandUseCase commands, GraphUseCase graph) {
    this.commands = commands;
    this.graph = graph;
  }

  @Transactional
  public void save(MovementForm form, boolean creating) {
    var key = new MovementKey(form.getKey());
    var content = form.toContent();
    var edges = form.toEdges(key.value());
    if (creating) commands.create(key, content);
    else commands.update(key, content);
    graph.setOutgoing(key.value(), edges);
  }
}
