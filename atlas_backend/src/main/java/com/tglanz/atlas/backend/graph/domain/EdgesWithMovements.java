package com.tglanz.atlas.backend.graph.domain;

import java.util.List;

/** The edges of a Movement, with the Movements its outgoing edges lead to, in no set order. */
public record EdgesWithMovements(MovementEdges edges, List<RelatedMovement> targets) {}
