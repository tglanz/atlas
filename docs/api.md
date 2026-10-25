# API

The web application talks to the backend over an HTTP API under `/api`. The controllers in each backend feature define the endpoints, and the endpoint reference is generated from them as OpenAPI: the page is at `/swagger-ui.html` and the raw description at `/v3/api-docs`, both for Maintainers only. This document records the conventions and rules behind them, not the endpoint list.

## Conventions

- JSON bodies.
- Errors are RFC 9457 problem details.
- Status codes carry meaning: 400 invalid input, 401 not signed in, 403 not allowed, 404 not found, 409 conflicts with the current state (duplicate, invalid transition, still in use).

## Authentication

- Signing in sets a session cookie. The web application is served from the same origin as the API, so cookies work without tokens in browser storage or CORS.
- Unsafe requests (anything that changes data) must send the value of the `XSRF-TOKEN` cookie in the `X-XSRF-TOKEN` header. The token changes when the session changes, for example on sign-in, so clients refresh it and retry once on a 403.

## Access

- Public: reading Movements, registering, and signing in.
- Signed-in users (practitioners): managing their own Routines. Another user's Routine looks like it does not exist, so its existence is not revealed.
- Maintainers: everything that creates or changes Movements, Illustrations, Avatars, and Viewpoints, and reading generation requests.
- Access is enforced on the backend for every request, independent of the UI.

## Rules

- A Movement's key is fixed when it is created and cannot be changed.
- Searching Movements by several tags returns those that have all of them.
- A Routine can only reference existing Movements.
- Relationships between Movements are managed through the graph, not the Movement. Both ends must exist, and a Movement cannot relate to itself.
- Generation is asynchronous. A request returns immediately and its status is polled.
- A generation makes one image for each stage of the Movement. Its images are only candidates: they replace the Movement's Illustrations when a maintainer approves them, and are dropped when discarded.
- A Movement has at most one generation in progress, and a request while one is running is a conflict.
- Saving a Movement never changes its Illustrations.
- An Avatar or Viewpoint that a generation request uses cannot be deleted.
