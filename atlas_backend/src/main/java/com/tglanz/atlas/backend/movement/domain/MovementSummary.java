package com.tglanz.atlas.backend.movement.domain;

import java.util.List;

/**
 * What a Movement listing shows: not the whole Movement, so it can be read without loading it. Tags
 * are by name, and the representative Illustration is the first one, if there is any.
 */
public record MovementSummary(
    MovementKey key, String name, List<String> tags, String representativeIllustration) {}
