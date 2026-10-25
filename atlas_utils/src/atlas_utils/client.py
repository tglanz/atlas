"""Read-only access to the public endpoints of a running Atlas app."""

from __future__ import annotations

import httpx

# A hosted instance that was asleep can take a while to answer the first request.
DEFAULT_TIMEOUT_SECONDS = 120.0


class AtlasClient:
    def __init__(
        self,
        base_url: str,
        *,
        timeout: float = DEFAULT_TIMEOUT_SECONDS,
        transport: httpx.BaseTransport | None = None,
    ) -> None:
        self._http = httpx.Client(
            base_url=base_url.rstrip("/"), timeout=timeout, transport=transport
        )

    def __enter__(self) -> AtlasClient:
        return self

    def __exit__(self, *exc_info: object) -> None:
        self._http.close()

    def _get(self, path: str):
        response = self._http.get(path)
        response.raise_for_status()
        return response.json()

    def movement_keys(self) -> list[str]:
        return [movement["key"] for movement in self._get("/api/movements")]

    def movement(self, key: str) -> dict:
        return self._get(f"/api/movements/{key}")

    def outgoing_edges(self, key: str) -> list[dict]:
        return self._get(f"/api/graph/movements/{key}/edges")["outgoing"]
