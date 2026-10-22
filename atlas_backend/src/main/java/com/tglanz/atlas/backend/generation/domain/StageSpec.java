package com.tglanz.atlas.backend.generation.domain;

/**
 * How one Illustration of the sequence is produced: generated with an Avatar, or a static image
 * used as is. The model allows a sequence to mix both; the UI decides which patterns to offer.
 */
public sealed interface StageSpec permits AvatarImageSpec, StaticImageSpec {}
