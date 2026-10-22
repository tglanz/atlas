package com.tglanz.atlas.backend.graph.domain;

/** A Movement reached by a traversal: how far from the start, and the edge it was reached by. */
public record Reach(String movement, int depth, Edge via) {}
