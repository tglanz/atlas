"""The command line. Each command only parses its input and calls a scenario."""

from __future__ import annotations

from pathlib import Path

import click
import httpx

from atlas_utils.client import AtlasClient
from atlas_utils.scenarios.export_movements import (
    NoMovementsError,
    UnsafeKeyError,
    export_movements,
)
from atlas_utils.scenarios.make_seed import InvalidExportError, make_seed

# The repository root: atlas_utils/src/atlas_utils/cli.py is three levels below it.
DEFAULT_MIGRATION_DIR = (
    Path(__file__).resolve().parents[3] / "atlas_backend/src/main/resources/db/migration"
)


@click.group()
def main() -> None:
    """Utilities for working with an Atlas deployment."""


@main.command("export-movements")
@click.argument("base_url")
@click.option(
    "--output-dir",
    "-o",
    type=click.Path(file_okay=False, path_type=Path),
    default=Path("export"),
    show_default=True,
    help="The directory to write one JSON file per movement into.",
)
@click.option(
    "--overwrite", is_flag=True, help="Fetch movements again even when their file exists."
)
def export_movements_command(base_url: str, output_dir: Path, overwrite: bool) -> None:
    """Export the movements of the app at BASE_URL, one JSON file named by key each.

    Movements whose file already exists are skipped. Only public endpoints are read, so no login
    is needed.
    """

    def report(key: str, skipped: bool) -> None:
        click.echo(f"{'Skipping' if skipped else 'Exporting'} {key}", err=True)

    try:
        with AtlasClient(base_url) as client:
            result = export_movements(client, output_dir, overwrite=overwrite, on_movement=report)
    except (NoMovementsError, UnsafeKeyError, httpx.HTTPError) as error:
        raise click.ClickException(str(error)) from error
    click.echo(
        f"Exported {len(result.exported)} movements to {output_dir}, skipped {len(result.skipped)}"
    )


@main.command("make-seed")
@click.argument("export_dir", type=click.Path(exists=True, file_okay=False, path_type=Path))
@click.option(
    "--out-dir",
    type=click.Path(file_okay=False, exists=True, path_type=Path),
    default=DEFAULT_MIGRATION_DIR,
    show_default=True,
    help="The migration directory to write into.",
)
def make_seed_command(export_dir: Path, out_dir: Path) -> None:
    """Turn an export directory (see export-movements) into seed migrations."""
    try:
        written = make_seed(export_dir, out_dir)
    except InvalidExportError as error:
        raise click.ClickException(str(error)) from error
    for path in written:
        click.echo(f"Wrote {path}")
    if len(written) == 1:
        click.echo("The export has no illustrations, so no illustrations migration was written.")
