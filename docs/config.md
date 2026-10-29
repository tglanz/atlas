# Site configuration

## Where to set values

- **Render:** service → **Environment** → add the variables below → save and redeploy. Render supplies `PORT` and `RENDER_EXTERNAL_URL` automatically.
- **Local:** export variables in the terminal before running `make run`, or prefix the command as below. The app does not automatically load `.env` files.
- **Docker:** pass variables using `docker run -e NAME=value ...` or `--env-file /path/to/private.env`.

```sh
ATLAS_GA_MEASUREMENT_ID=G-XXXXXXXXXX make run
```

Environment values override the defaults in [application.properties](../atlas_backend/src/main/resources/application.properties) and [application-postgres.properties](../atlas_backend/src/main/resources/application-postgres.properties). Restart the application after changing configuration. Keep passwords and API keys in the deployment environment or your local shell, outside source control.

## Public URL and server

| Variable | Default | Purpose |
| --- | --- | --- |
| `ATLAS_SITE_URL` | `RENDER_EXTERNAL_URL`, then `http://localhost:8080` | Public origin for canonical links, sitemap, previews and routine sharing. Include `https://`; exclude paths and trailing slash. Set this when using a custom domain. |
| `RENDER_EXTERNAL_URL` | Unset outside Render | Render's public service URL; used when `ATLAS_SITE_URL` is unset. |
| `PORT` | `8080` | Application listening port. Render sets it automatically. |
| `ATLAS_COOKIE_SECURE` | `false` | Set `true` when serving over HTTPS. Local HTTP uses `false`. |

## Google Analytics: visitors and pages

1. In [Google Analytics](https://analytics.google.com/), create an account/property and a **Web** data stream for the public site URL.
2. Copy the stream's **Measurement ID** (`G-…`), then set `ATLAS_GA_MEASUREMENT_ID` in Render and redeploy.
3. For basic traffic, enable **Page views** in the stream's Enhanced measurement settings. Other enhanced measurements are optional and can be turned off.
4. Visit the deployed site and check **Realtime** in GA. Standard reports provide users, views, pages and traffic sources.

| Variable | Default | Purpose |
| --- | --- | --- |
| `ATLAS_GA_MEASUREMENT_ID` | Empty: disabled | Enables the Google tag on full HTML pages. Empty means the tag is not loaded. |

This uses Google's ordinary browser tracking: no custom product events, account identifiers or server-side event forwarding. Google receives the visited URL and page title. User counts represent measured browsers/devices, rather than registered Atlas accounts; blocked scripts are not counted. Leave the ID unset locally to avoid mixing development traffic with production.

See [Google's setup guide](https://developers.google.com/analytics/devguides/collection/ga4/web).

## Coffee / support link

Create a creator page on Buy Me a Coffee, Ko-fi, or another support service. Complete payment/payout setup there, then paste the public creator-page URL into this variable if you want to override the default.

| Variable | Default | Purpose |
| --- | --- | --- |
| `ATLAS_COFFEE_URL` | `https://buymeacoffee.com/tglanz` | Full HTTPS support URL. Shows a **Buy me a coffee** link in the site footer. Set an empty value to hide it. |

Payments and fees are managed by the selected service. Atlas needs only the link, without payment credentials or API keys.

## Users and maintainer access

| Variable | Default | Purpose |
| --- | --- | --- |
| `ATLAS_ROOT_USER_EMAIL` | Empty | Email of a maintainer to create on startup. Set together with the password. |
| `ATLAS_ROOT_USER_PASSWORD` | Empty | Initial password for that new maintainer. |

Bootstrap runs only when both values are nonblank. If that email already exists, it does not change its password or role; these variables are not a password-reset mechanism. In an empty database, the first user who registers also becomes a maintainer; subsequent registrations create practitioners. Configure the maintainer before exposing a new deployment.

## Database

Migrations run automatically on startup. Choose SQLite or PostgreSQL.

### SQLite (default)

| Variable | Default | Purpose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Unset | Leave unset for SQLite. |
| `ATLAS_DB_SQLITE_PATH` | `./atlas.db` | SQLite file path, relative to the process working directory. `make run` starts inside `atlas_backend`; the container starts inside `/app`. |

Use an absolute path on a persistent volume for a deployed SQLite database. The parent directory must exist and be writable. An ephemeral service filesystem does not preserve database contents across replacements/redeploys.

### PostgreSQL

| Variable | Default | Purpose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Unset | Set `postgres` to select PostgreSQL. |
| `ATLAS_DB_POSTGRES_HOST` | Required | Database hostname. |
| `ATLAS_DB_POSTGRES_PORT` | `5432` | Database port. |
| `ATLAS_DB_POSTGRES_DATABASE` | Required | Database name. |
| `ATLAS_DB_POSTGRES_USER` | Required | Database user. |
| `ATLAS_DB_POSTGRES_PASSWORD` | Required | Database password. |
| `ATLAS_DB_POSTGRES_SSLMODE` | `require` | PostgreSQL TLS mode; use `disable` only for a local database without TLS. |

The application connects to an existing database; migrations create its tables. [deploy/postgres.Dockerfile](../deploy/postgres.Dockerfile) provides a disposable local PostgreSQL with example credentials. Its `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` initialize that database container; they do not configure the Atlas application.

## Illustration generation

| Variable | Default | Purpose |
| --- | --- | --- |
| `ATLAS_GENERATION_PROVIDER` | `stub` | `stub` returns a placeholder; `fal` calls fal.ai. |
| `ATLAS_FAL_AI_API_KEY` | Empty | fal.ai API key; required for real generation with `fal`. |
| `ATLAS_GENERATION_FAL_MODEL` | `openai/gpt-image-2` | fal.ai model endpoint. Alternatives must accept the connector's request format. |
| `ATLAS_GENERATION_FAL_IMAGE_SIZE` | `square_hd` | Image size supported by that model. |
| `ATLAS_GENERATION_FAL_QUALITY` | `high` | Quality supported by that model. |
| `ATLAS_GENERATION_FAL_TIMEOUT` | `5m` | Maximum wait for generation. |
| `ATLAS_GENERATION_FAL_POLL_INTERVAL` | `10s` | Delay between provider status checks. |
| `ATLAS_GENERATION_STUB_IMAGE_URL` | Elementor placeholder image URL | Image returned by the stub. Exact URL is in application.properties. |
| `ATLAS_GENERATION_STUB_MIN_DELAY` | `1s` | Minimum simulated generation delay. |
| `ATLAS_GENERATION_STUB_MAX_DELAY` | `4s` | Maximum simulated generation delay. |

For real generation, set `ATLAS_GENERATION_PROVIDER=fal` and `ATLAS_FAL_AI_API_KEY`. Duration values accept units such as `500ms`, `10s`, and `5m`.

Maintainers edit avatars and viewpoints under **Illustrations** in the site navigation; those settings live in the database. Movement stages are edited on each movement. Prompt templates live under [generation resources](../atlas_backend/src/main/resources/generation/); changing templates requires a rebuild. See [generation templates](specs/generation-template.md).

## Sharing preview images

The first illustration is served as a smaller JPEG for social previews. The original stays unchanged in storage and on movement pages. Resizing happens on the first image request; a bounded memory cache and public HTTP caching reuse the result. Restarting the app clears its memory cache. Normal page rendering does not fetch or resize images.

| Variable | Default | Purpose |
| --- | --- | --- |
| `ATLAS_PREVIEW_ALLOWED_HOSTS` | `fal.media` | Comma-separated image hosts the server may download for previews, including their subdomains. Empty disables resizing. URLs on other hosts stay as original preview URLs. Add only trusted image hosts; redirects are not followed. |

## Other application settings

These are Spring properties already defined by the app. Prefer the variables above for ordinary deployment. For an advanced override, use a Spring command-line argument, for example:

```sh
cd atlas_backend
java -jar build/libs/atlas_backend.jar --server.compression.enabled=false
```

| Property | Default | Purpose |
| --- | --- | --- |
| `spring.application.name` | `atlas_backend` | Application name. |
| `server.compression.enabled` | `true` | Compress HTTP responses. |
| `server.servlet.session.cookie.same-site` | `lax` | Session cookie SameSite behavior. |
| `logging.level.com.tglanz.atlas.backend` | `INFO` | Application logging level. |
| `logging.level.com.tglanz.atlas.backend.generation` | `DEBUG` | Generation logging; includes prompts/provider responses, not the authorization header. Set `INFO` to reduce detail. |
| `spring.datasource.url` | Built from the database variables above | Direct JDBC URL override, if needed. |
| `spring.datasource.username`, `spring.datasource.password` | From PostgreSQL variables in that profile | Direct database credential overrides. |
| `spring.datasource.driver-class-name` | SQLite driver; PostgreSQL driver in `postgres` profile | JDBC driver selected with the database. |
| `spring.datasource.hikari.maximum-pool-size` | `1` for SQLite; `10` for PostgreSQL | Database connection pool size. |
| `spring.jpa.database-platform` | SQLite dialect; PostgreSQL dialect in `postgres` profile | Hibernate dialect selected with the database. |
| `spring.jpa.hibernate.ddl-auto` | `none` | Flyway owns schema changes. |
| `spring.jpa.open-in-view` | `false` | Database work stays outside template rendering. |
| `spring.flyway.locations` | `classpath:db/migration` | Migration resources. |
| `springdoc.paths-to-match` | `/api/**` | API documentation scope. |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | API reference UI location; access requires a maintainer. |

Standard Spring environment overrides also work, such as `LOGGING_LEVEL_COM_TGLANZ_ATLAS_BACKEND=INFO` or `SPRING_DATASOURCE_URL=...`. Custom variables above map to the `atlas.*` settings in application.properties; direct `--atlas.…=…` arguments can override those properties too.

## Search discovery

Verify site ownership in [Google Search Console](https://search.google.com/search-console/about) and submit `/sitemap.xml` using the public origin. Verification is configured in Search Console/DNS, separately from GA. Atlas has no Search Console credential setting.

[Render Free services return crawler-blocking robots.txt while asleep](https://render.com/docs/free#automatic-robotstxt-responses). The app's crawler settings apply once requests reach the running application. No keepalive is configured.
