# atlas_ui

React, TypeScript, Vite frontend for Atlas. See `../docs/tooling.md` for versions.

## Commands

Run from this directory.

| Purpose                       | Command             |
| ----------------------------- | ------------------- |
| Install                       | `pnpm install`      |
| Dev server                    | `pnpm dev`          |
| Format                        | `pnpm format`       |
| Check formatting              | `pnpm format:check` |
| Lint                          | `pnpm lint`         |
| Build (type-check and bundle) | `pnpm build`        |
| Test                          | `pnpm test`         |

## Development

Start the backend (`./gradlew bootRun` in `atlas_backend`), then `pnpm dev`. Vite proxies `/api` to the backend on port 8080; set `VITE_DEV_API` to change it. See `ARCHITECTURE.md` for how the code is organized.

## Deployment

In production the backend serves the built UI, so both share one origin. `atlas_backend`'s Gradle build runs `pnpm build` and packages `dist` into the jar.
