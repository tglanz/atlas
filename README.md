# atlas

Form Atlas: a visual library of human movements, with routines built from them.

- `atlas_backend`: Spring Boot API. It also serves the built UI in production.
- `atlas_ui`: React web app.
- `atlas_utils`: command line utilities for a deployment, such as exporting its data into seed migrations.
- `docs`: product, domain, API and tooling documentation. Start with `docs/abstract.md`.

## Commands

Run these from the top directory. `make help` lists them.

```
make run      # build the jar and run the app (backend serving the UI)
make build    # build the single runnable jar, UI included
make test     # backend and UI tests
make format   # format all code
```

Each project's README has the tool-specific commands.
