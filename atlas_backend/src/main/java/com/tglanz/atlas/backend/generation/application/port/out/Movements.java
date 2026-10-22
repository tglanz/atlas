package com.tglanz.atlas.backend.generation.application.port.out;

import java.util.List;
import java.util.Optional;

public interface Movements {

  record Info(String name, String description) {}

  Optional<Info> find(String movementKey);

  /** Replaces the Movement's whole sequence of Illustrations. */
  void replaceIllustrations(String movementKey, List<String> urls);
}
