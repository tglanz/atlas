package com.tglanz.atlas.backend.movement.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record CreateMovementRequest(@NotBlank String key, @NotNull @Valid MovementRequest content) {}
