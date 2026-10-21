package com.tglanz.atlas.backend.generation.application.port.out;

import com.tglanz.atlas.backend.generation.domain.Stage;
import java.util.List;

/** Everything the generator needs to illustrate one sequence. */
public record GenerationInput(
    String movementName,
    String movementDescription,
    String avatarSpecification,
    String viewpointSpecification,
    List<Stage> stages) {}
