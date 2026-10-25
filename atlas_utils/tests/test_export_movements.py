import json

import httpx
import pytest

from atlas_utils.client import AtlasClient
from atlas_utils.scenarios.export_movements import (
    NoMovementsError,
    UnsafeKeyError,
    export_movements,
)

EDGE = {"source": "push_up", "target": "pike_pushup", "type": "progression"}


class FakeApp:
    """Serves two movements and counts the requests made for each path."""

    def __init__(self) -> None:
        self.requests: list[str] = []
        self.routes = {
            "/api/movements": [{"key": "push_up"}, {"key": "pike_pushup"}],
            "/api/movements/push_up": {"key": "push_up", "name": "Push up"},
            "/api/movements/pike_pushup": {"key": "pike_pushup", "name": "Pike pushup"},
            "/api/graph/movements/push_up/edges": {"outgoing": [EDGE], "incoming": []},
            "/api/graph/movements/pike_pushup/edges": {"outgoing": [], "incoming": []},
        }

    def client(self) -> AtlasClient:
        def handler(request: httpx.Request) -> httpx.Response:
            self.requests.append(request.url.path)
            if request.url.path not in self.routes:
                return httpx.Response(404)
            return httpx.Response(200, json=self.routes[request.url.path])

        return AtlasClient("http://atlas.test", transport=httpx.MockTransport(handler))


def test_each_movement_gets_a_file_named_by_key(tmp_path):
    seen: list[tuple[str, bool]] = []

    result = export_movements(
        FakeApp().client(), tmp_path / "export", on_movement=lambda *args: seen.append(args)
    )

    assert result.exported == ["push_up", "pike_pushup"]
    assert result.skipped == []
    assert seen == [("push_up", False), ("pike_pushup", False)]
    assert json.loads((tmp_path / "export" / "push_up.json").read_text()) == {
        "key": "push_up",
        "name": "Push up",
        "edges": [EDGE],
    }


def test_existing_movements_are_skipped_without_being_fetched(tmp_path):
    directory = tmp_path / "export"
    directory.mkdir()
    (directory / "push_up.json").write_text("kept")
    app = FakeApp()

    result = export_movements(app.client(), directory, on_movement=lambda *args: None)

    assert result.exported == ["pike_pushup"]
    assert result.skipped == ["push_up"]
    assert (directory / "push_up.json").read_text() == "kept"
    assert "/api/movements/push_up" not in app.requests


def test_overwrite_fetches_existing_movements_again(tmp_path):
    directory = tmp_path / "export"
    directory.mkdir()
    (directory / "push_up.json").write_text("old")

    result = export_movements(FakeApp().client(), directory, overwrite=True)

    assert result.skipped == []
    assert json.loads((directory / "push_up.json").read_text())["name"] == "Push up"


def test_a_failed_request_leaves_no_partial_file(tmp_path):
    app = FakeApp()
    del app.routes["/api/graph/movements/pike_pushup/edges"]

    with pytest.raises(httpx.HTTPStatusError):
        export_movements(app.client(), tmp_path / "export")

    assert sorted(path.name for path in (tmp_path / "export").iterdir()) == ["push_up.json"]


def test_fails_when_there_are_no_movements(tmp_path):
    app = FakeApp()
    app.routes["/api/movements"] = []

    with pytest.raises(NoMovementsError):
        export_movements(app.client(), tmp_path / "export")


def test_refuses_a_key_that_is_not_a_safe_file_name(tmp_path):
    app = FakeApp()
    app.routes["/api/movements"] = [{"key": "../evil"}]

    with pytest.raises(UnsafeKeyError):
        export_movements(app.client(), tmp_path / "export")
