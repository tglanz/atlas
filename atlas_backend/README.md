# atlas_backend

Spring Boot API, built with Gradle.

## Commands

```
./gradlew build           # compile, check formatting, run tests, bundle the UI
./gradlew test            # tests only
./gradlew spotlessApply   # format the code
./gradlew bootRun         # run the API on :8080
./gradlew bootJar         # runnable jar in build/libs, UI included
```

`build` and `bootJar` also build `../atlas_ui`, so they need Node and pnpm. Add `-PskipUi` to leave the UI out.

## Database

SQLite by default, in `atlas.db` (set `ATLAS_DB_PATH` to move it). Migrations in `src/main/resources/db/migration` run automatically on startup; there is no separate migration command. To start over, delete `atlas.db`.

Migrations are still edited in place until the first deployment. If startup fails with a Flyway checksum error after pulling, delete `atlas.db`.

For PostgreSQL, set `SPRING_PROFILES_ACTIVE=postgres`, `ATLAS_DB_URL`, `ATLAS_DB_USER` and `ATLAS_DB_PASSWORD`.

## More

- `ARCHITECTURE.md`: how the code is organized.
- `../docs/api.md`: the API conventions and rules.
