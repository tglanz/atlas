import json
from pathlib import Path

import pytest

MIGRATIONS = Path(__file__).resolve().parents[2] / "atlas_backend/src/main/resources/db/migration"


@pytest.fixture
def export_data() -> list[dict]:
    return [
        {
            "key": "push_up",
            "name": "Push up",
            "description": "It's a push-up.",
            "items": [{"type": "CUE", "content": "Hands under the shoulders"}],
            "stages": [
                {"name": "Eccentric", "description": "Chest to floor."},
                {"name": "Concentric", "description": "Elbows locked."},
            ],
            "tags": ["push", "chest"],
            "illustrations": ["https://example.com/a.png", "https://example.com/b.png"],
            "edges": [{"source": "push_up", "target": "pike_pushup", "type": "progression"}],
        },
        {
            "key": "pike_pushup",
            "name": "Pike pushup",
            "description": "Hips high.",
            "items": [],
            "stages": [],
            "tags": [],
            "illustrations": [],
            "edges": [{"source": "pike_pushup", "target": "push_up", "type": "regression"}],
        },
    ]


def write_export(directory: Path, movements: list[dict]) -> Path:
    directory.mkdir(parents=True, exist_ok=True)
    for movement in movements:
        (directory / f"{movement['key']}.json").write_text(json.dumps(movement), encoding="utf-8")
    return directory


@pytest.fixture
def export_dir(tmp_path: Path, export_data: list[dict]) -> Path:
    return write_export(tmp_path / "export", export_data)
