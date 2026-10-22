package com.tglanz.atlas.backend.generation.application.port.out;

import java.util.List;
import java.util.Optional;

public interface Movements {

  /** What generation needs of a Movement. Cues are in the Movement's order. */
  record Info(String name, String description, List<String> cues) {}

  Optional<Info> find(String movementKey);

  /** Replaces the Movement's whole sequence of Illustrations. */
  void replaceIllustrations(String movementKey, List<String> urls);
}
