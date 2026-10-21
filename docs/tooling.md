# Tooling

## Runtimes

| Tool | Version |
| --- | --- |
| Java (JDK) | 25 (LTS) |
| Node.js | 24.21.0 |
| pnpm | 12.3.4 |

## Backend: `atlas_backend`

- Build tool: Gradle 9.8.2 (Groovy DSL, via wrapper `./gradlew`)
- Spring Boot 4.1.1
- Group: `com.tglanz.atlas`; base package: `com.tglanz.atlas.backend`
- Persistence: Spring Data JPA (Hibernate), Flyway; SQLite (default, xerial `sqlite-jdbc` 3.53.4.0) or PostgreSQL via the `postgres` profile
- Formatter: Spotless with google-java-format (`./gradlew spotlessApply`)
- Lint: javac `-Xlint:all -Werror`
- Architecture rules: ArchUnit 1.5.1 (`ArchitectureTest`)
- Security: Spring Security (session login, CSRF cookie, BCrypt)
- Validation: Spring Boot validation starter
- Tests: JUnit Platform, Spring Boot test starters, in-memory SQLite

Commands, from `atlas_backend/`:

| Purpose | Command |
| --- | --- |
| Build and test | `./gradlew build` |
| Run tests | `./gradlew test` |
| Run a single test class | `./gradlew test --tests 'com.tglanz.atlas.backend.SomeTest'` |
| Compile only | `./gradlew compileJava compileTestJava` |
| Run the application | `./gradlew bootRun` |
| Format | `./gradlew spotlessApply` |
| Check formatting | `./gradlew spotlessCheck` |
| Build executable jar | `./gradlew bootJar` |
| Clean | `./gradlew clean` |
| List dependencies | `./gradlew dependencies` |

## Frontend: `atlas_ui`

- Package manager: pnpm
- Build tool: Vite 8.3.4
- Language: TypeScript 6.0.3
- Libraries:
  - React 19.3.1, React DOM 19.3.1
  - React Router 8.5.0
  - TanStack Query 5.104.5
  - Zustand 5.0.15
  - Mantine 9.7.3 (`@mantine/core`, `@mantine/hooks`)
  - Tabler Icons 3.49.0 (`@tabler/icons-react`)
- Lint: oxlint 1.89.0
- Formatter: Prettier 3.9.10 (single quotes)
- Plugin: `@vitejs/plugin-react` 6.1.2

Commands, from `atlas_ui/`:

| Purpose | Command |
| --- | --- |
| Install dependencies | `pnpm install` |
| Dev server | `pnpm dev` |
| Type-check and build | `pnpm build` |
| Lint | `pnpm lint` |
| Format | `pnpm format` |
| Check formatting | `pnpm format:check` |
| Preview production build | `pnpm preview` |
| Add a dependency | `pnpm add <package>` |
| Add a dev dependency | `pnpm add -D <package>` |

Test runner: Vitest 5.0.5 (`pnpm test`).

## Dev container

`.devcontainer/devcontainer.json` provisions Java 25 (Temurin), Node.js 24.21.0, pnpm 12.3.4 and the GitHub CLI, installs frontend dependencies, and compiles the backend. Gradle comes from the wrapper.

## CI

`.github/workflows/ci.yml` builds and tests both projects on push to `main` and on pull requests.

`.github/workflows/deploy-ui.yml` builds `atlas_ui` on pushes to `main` that change it and publishes the static site to the `gh-pages` branch. GitHub Pages serves that branch at `https://<owner>.github.io/<repo>/`. The workflow sets `VITE_BASE` to the repository path, and copies `index.html` to `404.html` so deep links reach the app.
