package com.tglanz.atlas.backend.graph.domain;

import java.util.List;

/** What is shown of a Movement that another one is related to. */
public record RelatedMovement(
    String key, String name, List<String> tags, String representativeIllustration) {}
