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

Public URLs use `ATLAS_SITE_URL` when set, otherwise Render’s `RENDER_EXTERNAL_URL`, then the local development origin. Set `ATLAS_SITE_URL` to the canonical public HTTP(S) origin when using a custom domain, including its scheme and excluding a path. This configuration controls canonical links, social previews, sitemap entries and routine share links; the incoming request host does not determine public URLs. Changing the hostname requires configuration, not a code change.

For search discovery, submit the deployed sitemap in Google Search Console and ensure the hosting layer allows search crawlers to fetch public pages and images. [Render Free services return a crawler-blocking robots.txt while asleep](https://render.com/docs/free#automatic-robotstxt-responses); application crawler settings cannot override that hosting behavior. [OpenAI's crawler documentation](https://developers.openai.com/api/docs/bots) describes ChatGPT search access. Search visibility is not guaranteed by metadata or a sitemap.

When serving over HTTPS, set `ATLAS_COOKIE_SECURE=true`. Choose the database profile and supply its credentials through the environment. Configure a maintainer through `ATLAS_ROOT_USER_EMAIL` and `ATLAS_ROOT_USER_PASSWORD`; keep those values and provider credentials out of source control. The configuration resources define the remaining environment options and defaults.

## More

See [architecture](ARCHITECTURE.md), [API conventions](../docs/api.md), and [tooling](../docs/tooling.md).
