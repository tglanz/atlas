package com.tglanz.atlas.backend.movement.domain;

import java.util.List;

/**
 * A Movement's current practice instructions, with Illustrations and cues in their stored order.
 */
public record MovementInstructions(
    MovementKey key, String name, List<Illustration> illustrations, List<String> cues) {
  public MovementInstructions {
    illustrations = List.copyOf(illustrations);
    cues = List.copyOf(cues);
  }
}
