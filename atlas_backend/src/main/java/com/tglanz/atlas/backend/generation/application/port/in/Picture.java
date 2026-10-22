package com.tglanz.atlas.backend.generation.application.port.in;

/** A picture received from a user. */
public record Picture(String contentType, byte[] content) {}
