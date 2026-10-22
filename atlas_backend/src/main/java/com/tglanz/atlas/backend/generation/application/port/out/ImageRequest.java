package com.tglanz.atlas.backend.generation.application.port.out;

/** What the generator needs to produce one picture. Instructions or the reference may be empty. */
public record ImageRequest(
    String movementName,
    String movementDescription,
    String avatarSpecification,
    String viewpointSpecification,
    String instructions,
    String referenceImageUrl) {}
