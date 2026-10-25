"""Exports the movements of a running app into a directory, one JSON file per movement."""

from __future__ import annotations

import json
import os
import re
from collections.abc import Callable
from dataclasses import dataclass, field
from pathlib import Path

from atlas_utils.client import AtlasClient

# Movement keys are lowercase slugs. Checking keeps a file name from leaving the directory.
SAFE_KEY = re.compile(r"^[a-z0-9_-]+$")


class NoMovementsError(Exception):
    """The app has no movements, so there is nothing to export."""


class UnsafeKeyError(Exception):
    """The app returned a movement key that is not usable as a file name."""


@dataclass
class ExportResult:
    exported: list[str] = field(default_factory=list)
    skipped: list[str] = field(default_factory=list)


def movement_file(directory: Path, key: str) -> Path:
    return directory / f"{key}.json"


def _write_atomically(path: Path, data: dict) -> None:
    """A file either exists complete or not at all, so an interrupted export can be resumed."""
    temporary = path.with_suffix(".json.tmp")
    temporary.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    os.replace(temporary, path)


def export_movements(
    client: AtlasClient,
    directory: Path,
    *,
    overwrite: bool = False,
    on_movement: Callable[[str, bool], None] | None = None,
) -> ExportResult:
    """Writes <key>.json for each movement: its details, stages, tags, illustrations and outgoing
    relationships. Relationships are stored in both directions, so the outgoing ones of every
    movement are all of them.

    A movement whose file exists is skipped, without being fetched, unless overwrite is set.
    on_movement is called with each key and whether it is skipped.
    """
    keys = client.movement_keys()
    if not keys:
        raise NoMovementsError("The app has no movements.")
    for key in keys:
        if not SAFE_KEY.match(key):
            raise UnsafeKeyError(f"Cannot use the movement key {key!r} as a file name.")

    directory.mkdir(parents=True, exist_ok=True)
    result = ExportResult()
    for key in keys:
        path = movement_file(directory, key)
        skip = path.exists() and not overwrite
        if on_movement:
            on_movement(key, skip)
        if skip:
            result.skipped.append(key)
            continue
        _write_atomically(path, {**client.movement(key), "edges": client.outgoing_edges(key)})
        result.exported.append(key)
    return result
