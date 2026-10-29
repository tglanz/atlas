# Tooling

## Runtimes

- Java 21 (LTS) minimum; Java 25 is also supported. Gradle uses the current JDK and targets Java 21 bytecode and APIs.
- Python 3.14, with uv 0.13 managing the runtime and packages for the utilities.

Gradle comes from the wrapper. Library and plugin versions are defined in the application build and the utilities' package configuration; those files are the source of truth.

## Build and checks

The application build packages Java, HTML templates, styles and browser scripts together. It does not require a JavaScript package manager or a separate asset build. Spring serves the application during local development as well as deployment, so authentication and page rendering use the same origin in both environments.

Spotless checks Java formatting, and compiler warnings fail the build. ArchUnit checks enforce the [architecture rules](../atlas_backend/ARCHITECTURE.md). The utilities use pytest and Ruff and are not part of the deployed application.

## Commands and environments

Use `make help` at the repository root for common commands. Tool-specific commands live in the [application README](../atlas_backend/README.md) and [utilities README](../atlas_utils/README.md).

The [dev container configuration](../.devcontainer/devcontainer.json) defines the development environment. The [CI workflow](../.github/workflows/ci.yml) defines the required automated checks. Keep their installation and execution details in those files rather than duplicating them here.
