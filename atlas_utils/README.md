# atlas_utils

```
uv sync                  # install
uv run pytest            # test
uv run ruff format .     # format
uv run ruff check .      # lint
uv run atlas-utils --help
```

## Seeding from a deployment

The development database is disposable. Refresh its seed baseline from a deployment, then recreate it locally:

```
uv run atlas-utils export-movements https://your-app.example.com -o export
uv run atlas-utils make-seed export
```

The export includes public movements, items, stages, tags, illustration URLs and relationships. It does not copy accounts, routines, generation history or maintainer settings. Images stay at their existing URLs, with stage associations preserved for partially illustrated Movements.

Use a fresh export directory to capture the current catalog without leaving files for removed movements. Existing files are skipped so an interrupted export can resume; `--overwrite` fetches them again.

`make-seed` replaces the full movement seed and writes an illustration seed when images exist. The output names are defined in [make_seed.py](src/atlas_utils/scenarios/make_seed.py); keep their versions distinct from the other migrations. Use `--out-dir DIRECTORY` to inspect generated SQL elsewhere first.

After reviewing the generated SQL, stop the local application, remove its SQLite database and restart it. The seeds are a fresh baseline, not incremental data updates. See [site configuration](../docs/config.md) for database paths and deployment settings.
