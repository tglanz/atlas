package com.tglanz.atlas.backend.graph.domain;

import java.util.List;

/** The edges leaving and entering one Movement. */
public record MovementEdges(List<Edge> outgoing, List<Edge> incoming) {}
