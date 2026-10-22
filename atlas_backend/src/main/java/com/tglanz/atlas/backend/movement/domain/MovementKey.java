package com.tglanz.atlas.backend.movement.domain;

import java.util.regex.Pattern;

/**
 * Stable, URL-safe identifier of a Movement: lowercase words separated by hyphens or underscores.
 */
public record MovementKey(String value) {
  private static final Pattern FORMAT = Pattern.compile("[a-z0-9]+([_-][a-z0-9]+)*");
  private static final int MAX_LENGTH = 100;

  public MovementKey {
    if (value == null || value.length() > MAX_LENGTH || !FORMAT.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "Movement key must be lowercase letters, digits, hyphens and underscores (max 100 characters)");
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
