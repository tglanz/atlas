package com.tglanz.atlas.backend.shared.persistence;

import java.sql.SQLException;

/** Detects database constraint violations independent of the database vendor. */
public final class ConstraintViolations {
  private static final int SQLITE_CONSTRAINT = 19;

  private ConstraintViolations() {}

  public static boolean isConstraintViolation(Throwable error) {
    for (Throwable t = error; t != null; t = t.getCause()) {
      if (t instanceof SQLException e) {
        String state = e.getSQLState();
        // PostgreSQL: SQLState class 23. SQLite: primary result code 19 (SQLITE_CONSTRAINT).
        if ((state != null && state.startsWith("23"))
            || (e.getErrorCode() & 0xFF) == SQLITE_CONSTRAINT) {
          return true;
        }
      }
      if (t.getCause() == t) {
        break;
      }
    }
    return false;
  }
}
