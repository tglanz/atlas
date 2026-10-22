# atlas

Form Atlas: a visual library of human movements, with routines built from them.

- `atlas_backend`: Spring Boot API. It also serves the built UI in production.
- `atlas_ui`: React web app.
- `docs`: product, domain, API and tooling documentation. Start with `docs/abstract.md`.

## Run locally

```
cd atlas_backend && ./gradlew bootRun    # API on :8080
cd atlas_ui && pnpm install && pnpm dev # UI on :5173, proxies /api to the backend
```

See each project's README for the other commands.
