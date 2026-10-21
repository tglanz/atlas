package com.tglanz.atlas.backend.routine.application.port.out;

public interface PublishedMovementCatalog {
  boolean isPublished(String key, int revision);
}
