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

## Deployment

The static site is published to the `gh-pages` branch by `.github/workflows/deploy-ui.yml`. To build it locally for a project site, run `VITE_BASE=/atlas/ pnpm build`.
