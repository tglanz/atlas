import sqlite3
from pathlib import Path

import pytest

from atlas_utils.scenarios.make_seed import (
    ILLUSTRATIONS_FILE,
    MOVEMENTS_FILE,
    InvalidExportError,
    make_seed,
)
from tests.conftest import MIGRATIONS, write_export


def load_into_database(*files: Path) -> sqlite3.Connection:
    """A database with the real schema and the given seed files applied."""
    connection = sqlite3.connect(":memory:")
    connection.executescript((MIGRATIONS / "V1__domain.sql").read_text())
    for file in files:
        connection.executescript(file.read_text())
    return connection


def test_seed_loads_into_the_schema_and_keeps_everything(tmp_path, export_dir):
    written = make_seed(export_dir, tmp_path)

    assert [path.name for path in written] == [MOVEMENTS_FILE, ILLUSTRATIONS_FILE]
    db = load_into_database(*written)
    assert db.execute(
        "select name, description from movement where movement_key='push_up'"
    ).fetchone() == (
        "Push up",
        "It's a push-up.",
    )
    assert db.execute(
        "select name from movement_stage where movement_key='push_up' order by position"
    ).fetchall() == [
        ("Eccentric",),
        ("Concentric",),
    ]
    assert db.execute(
        "select tag from movement_tag where movement_key='push_up' order by tag"
    ).fetchall() == [
        ("chest",),
        ("push",),
    ]
    assert db.execute("select count(*) from movement_edge").fetchone() == (2,)
    assert db.execute(
        "select image_url from movement_illustration order by position"
    ).fetchall() == [
        ("https://example.com/a.png",),
        ("https://example.com/b.png",),
    ]


def test_output_is_the_same_whatever_the_order_of_the_movements(tmp_path, export_data):
    first, second = tmp_path / "first", tmp_path / "second"
    first.mkdir()
    second.mkdir()
    make_seed(write_export(tmp_path / "a", export_data), first)
    make_seed(write_export(tmp_path / "b", list(reversed(export_data))), second)

    assert (first / MOVEMENTS_FILE).read_text() == (second / MOVEMENTS_FILE).read_text()


def test_no_illustrations_migration_without_illustrations(tmp_path, export_data):
    for movement in export_data:
        movement["illustrations"] = []

    written = make_seed(write_export(tmp_path / "export", export_data), tmp_path)

    assert [path.name for path in written] == [MOVEMENTS_FILE]
    assert not (tmp_path / ILLUSTRATIONS_FILE).exists()


def test_rejects_a_movement_file_that_is_incomplete(tmp_path):
    bad = write_export(tmp_path / "bad", [{"key": "push_up"}])

    with pytest.raises(InvalidExportError):
        make_seed(bad, tmp_path)


def test_rejects_a_file_named_for_another_key(tmp_path, export_data):
    directory = write_export(tmp_path / "export", export_data)
    (directory / "push_up.json").rename(directory / "other.json")

    with pytest.raises(InvalidExportError):
        make_seed(directory, tmp_path)


def test_rejects_an_empty_directory(tmp_path):
    (tmp_path / "empty").mkdir()

    with pytest.raises(InvalidExportError):
        make_seed(tmp_path / "empty", tmp_path)


def test_illustration_seed_version_does_not_collide_with_another_migration():
    version = ILLUSTRATIONS_FILE.split("__")[0]
    assert all(path.name == ILLUSTRATIONS_FILE for path in MIGRATIONS.glob(f"{version}__*.sql"))
