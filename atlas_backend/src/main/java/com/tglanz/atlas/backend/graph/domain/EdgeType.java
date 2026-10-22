package com.tglanz.atlas.backend.graph.domain;

/**
 * A relationship type the user interface offers. A bidirectional type is stored as two edges, one
 * in each direction.
 */
public record EdgeType(String name, String label, boolean bidirectional) {}
