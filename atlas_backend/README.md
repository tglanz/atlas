# atlas_backend

Spring Boot backend for Atlas. See `../docs/tooling.md` for versions.

## Commands

Run from this directory.

| Purpose | Command |
| --- | --- |
| Format | `./gradlew spotlessApply` |
| Check formatting | `./gradlew spotlessCheck` |
| Lint | `./gradlew compileJava compileTestJava` (javac `-Xlint:all -Werror`) |
| Build (compile, format check, test, UI bundle) | `./gradlew build` (add `-PskipUi` to skip the UI and Node) |
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

### Migrations before the first deployment

Nothing is deployed yet, so existing migrations (`V1` to `V5`) are edited in place instead of adding new ones. Once a database exists anywhere that must be kept, stop editing applied migrations and add a new `V<n>__description.sql` instead.

Flyway stores a checksum of every migration it applies. If you ran an older version of the app locally and then pulled a changed migration, startup fails with a checksum or validation error. Delete the local database and start again:

```
rm atlas.db atlas.db-*      # in atlas_backend, or the file named by ATLAS_DB_PATH
./gradlew bootRun
```

This loses local data only. Tests use a fresh in-memory database and are not affected.
