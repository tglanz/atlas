package com.tglanz.atlas.backend.generation.application.port.out;

/** What the connector needs to make one image: a system prompt and an image prompt. */
public record ImageRequest(String systemPrompt, String imagePrompt) {}
