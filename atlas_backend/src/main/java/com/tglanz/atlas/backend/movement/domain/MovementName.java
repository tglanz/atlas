package com.tglanz.atlas.backend.movement.domain;

/** A Movement's name with its key, as listed for searching. */
public record MovementName(MovementKey key, String name) {}
