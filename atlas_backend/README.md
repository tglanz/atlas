# atlas_backend

Spring Boot backend for Atlas. See `../docs/tooling.md` for versions.

## Commands

Run from this directory.

| Purpose | Command |
| --- | --- |
| Format | `./gradlew spotlessApply` |
| Check formatting | `./gradlew spotlessCheck` |
| Lint | `./gradlew compileJava compileTestJava` (javac `-Xlint:all -Werror`) |
| Build (compile, format check, test) | `./gradlew build` |
| Test | `./gradlew test` |
| Run | `./gradlew bootRun` |

## Stubs

Picture storage (S3) and Illustration generation are stubs: every stored or generated picture gets the url in `atlas.storage.stub-url`.

## Database

Auth: session cookie login with users stored in the database. The first registered user is a Maintainer. Set `ATLAS_COOKIE_SECURE=true` behind HTTPS.

SQLite by default (file `./atlas.db`, override with `ATLAS_DB_PATH`). Tests use in-memory SQLite.

To use PostgreSQL, activate the `postgres` profile and set the connection:

```
SPRING_PROFILES_ACTIVE=postgres
ATLAS_DB_URL=jdbc:postgresql://host:5432/atlas
ATLAS_DB_USER=...
ATLAS_DB_PASSWORD=...
```

Architecture rules are in `ARCHITECTURE.md` and enforced by `ArchitectureTest`. API is in `../docs/api.md`.

Flyway migrations live in `src/main/resources/db/migration` and must stay portable across both databases.
