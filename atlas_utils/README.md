# atlas_utils

```
uv sync                  # install
uv run pytest            # test
uv run ruff format .     # format
uv run ruff check .      # lint
uv run atlas-utils --help
```

## Seeding from a deployment

Export the movements of a running app, then turn the export into the seed migrations:

```
uv run atlas-utils export-movements https://your-app.example.com -o export
uv run atlas-utils make-seed export
```

`export-movements` writes one JSON file per movement, named by key, into the directory and skips movements whose file already exists, so an interrupted export can be resumed. Add `--overwrite` to fetch them all again.

`make-seed` reads the directory and replaces `V5__seed_movements.sql` and, when the export has illustrations, `V6__seed_illustrations.sql` in the backend's migration directory. Review the diff, then reset the databases, since changed migrations no longer match Flyway's checksums. The export reads only public endpoints.
