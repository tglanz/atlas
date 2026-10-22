# Tooling

## Runtimes

- Java 25 (LTS)
- Node.js 24
- pnpm 12

Gradle comes from the wrapper. Library and plugin versions are defined in `atlas_backend/build.gradle` and `atlas_ui/package.json`, which are the source of truth.

## Backend

- Gradle builds the project. Flyway runs database migrations on startup.
- Spotless formats the code. The compiler treats warnings as errors.
- ArchUnit tests enforce the layering rules in `atlas_backend/ARCHITECTURE.md`.

## Frontend

- Vite builds and serves the app. Vitest runs tests.
- Prettier formats the code. oxlint lints it, including the import rules in `atlas_ui/ARCHITECTURE.md`.

## How the pieces fit

- The backend build also builds the frontend and packages it into the backend jar. This needs Node and pnpm, unless the build is told to skip the UI.
- The app normally runs as that single jar. The frontend also has its own dev server, which proxies `/api` to the backend, for faster UI iteration.

## Commands

Common tasks (`run`, `build`, `test`, `format`) are `make` targets at the top directory; `make help` lists them. They only call the projects' own tools. Tool-specific commands are in the README of each project.

## Dev container

`.devcontainer/devcontainer.json` provides the runtimes above and prepares both projects.

## CI

`.github/workflows/ci.yml` builds and tests both projects on pushes to `main` and on pull requests.
