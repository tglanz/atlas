# API

The HTTP API under `/api` serves programmatic clients. HTML pages use the same application use cases directly. The controllers in each backend feature define the API, and its reference is generated from them as OpenAPI: the page is at `/swagger-ui.html` and the raw description at `/v3/api-docs`, both for Maintainers only. This document records the conventions and rules behind it rather than duplicating the generated reference.

## Conventions

- JSON bodies.
- Errors are RFC 9457 problem details.
- Status codes carry meaning: 400 invalid input, 401 not signed in, 403 not allowed, 404 not found, 409 conflicts with the current state (duplicate, invalid transition, still in use).

## Authentication

- Signing in sets a session cookie. The web application is served from the same origin as the API, so cookies work without tokens in browser storage or CORS.
- Unsafe requests (anything that changes data) require a CSRF token. JSON clients send the value of the `XSRF-TOKEN` cookie in the `X-XSRF-TOKEN` header; HTML forms include the server's token as a hidden field. Session changes rotate the token, so clients must refresh it after signing in or out.

## Access

- Public: reading Movements, registering, and signing in.
- Signed-in users (practitioners): managing their own Routines. Another user's private Routine URL looks like it does not exist, so its existence is not revealed. Public Routine links are read-only HTML views and do not grant access to the owner APIs.
- Maintainers: everything that creates or changes Movements, Illustrations, Avatars, and Viewpoints, and reading generation requests.
- Access is enforced on the backend for every request, independent of the UI.

## Rules

- A Movement's key is fixed when it is created and cannot be changed.
- Searching Movements by several tags returns those that have all of them.
- A Routine can only reference existing Movements.
- Relationships between Movements are managed through the graph, not the Movement. Both ends must exist, and a Movement cannot relate to itself.
- Generation is asynchronous. A request returns immediately and its status is polled.
- A generation captures its inputs, optional guidelines and preset specifications, and generates all stages or a selected stage. Input overrides never update the saved Movement. Images are candidates until approved; approving a single stage preserves other Illustrations. Saved stage changes invalidate approval so images cannot be applied to the wrong position.
- A Movement has at most one generation in progress, and a request while one is running is a conflict.
- Saving a Movement never changes its Illustrations.
- An Avatar or Viewpoint that a generation request uses cannot be deleted.
