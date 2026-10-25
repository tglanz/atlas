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

Database configuration is defined in [application.properties](src/main/resources/application.properties) and its profile overrides. Migrations run automatically on startup.

For a disposable local database, stop the application and remove the database file before starting again. Never use that procedure to resolve migration failures on a database whose contents must be preserved.

## Deployment

Build the container from the repository root:

```
docker build -f deploy/app.Dockerfile -t atlas .
```

Set `ATLAS_SITE_URL` to the actual public HTTP(S) origin of the deployment, including its scheme and excluding a path. Canonical links, social metadata and sitemap entries use this trusted value; the incoming request host does not determine public URLs.

When serving over HTTPS, set `ATLAS_COOKIE_SECURE=true`. Choose the database profile and supply its credentials through the environment. Configure a maintainer through `ATLAS_ROOT_USER_EMAIL` and `ATLAS_ROOT_USER_PASSWORD`; keep those values and provider credentials out of source control. The configuration resources define the remaining environment options and defaults.

## More

See [architecture](ARCHITECTURE.md), [API conventions](../docs/api.md), and [tooling](../docs/tooling.md).
