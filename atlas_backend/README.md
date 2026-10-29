# atlas_backend

## Commands

Run these from this directory.

```
./gradlew build           # compile, check formatting, run tests, package the application
./gradlew test            # tests only
./gradlew spotlessApply   # format Java code
./gradlew bootRun         # run the application
./gradlew bootJar         # runnable jar in build/libs
```

## Database

See [site configuration](../docs/config.md) for database selection, credentials and persistence. Migrations run automatically on startup.

For a disposable local database, stop the application and remove the database file before starting again. Never use that procedure to resolve migration failures on a database whose contents must be preserved.

## Deployment

Build the container from the repository root:

```
docker build -f deploy/app.Dockerfile -t atlas .
```

See [site configuration](../docs/config.md) for environment variables, public URLs, analytics, coffee links, maintainer access, generation, databases and search discovery. Set deployment values in the service environment and restart/redeploy.

## More

See [architecture](ARCHITECTURE.md), [API conventions](../docs/api.md), and [tooling](../docs/tooling.md).
