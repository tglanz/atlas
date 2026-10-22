package com.tglanz.atlas.backend.graph.domain;

/**
 * A relationship type the user interface offers. When an edge of this type is added, the edge of
 * the inverse type is added in the opposite direction. A type that is its own inverse, such as
 * variant, gives two edges of the same type. A null inverse means the type is one-way.
 */
public record EdgeType(String name, String label, String plural, String inverse) {}
