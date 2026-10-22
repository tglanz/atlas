# API

The web application talks to the backend over an HTTP API under `/api`. The controllers in each backend feature define the endpoints. This document records the conventions and rules behind them, not the endpoint list.

## Conventions

- JSON bodies, except picture uploads, which are multipart.
- Errors are RFC 9457 problem details.
- Status codes carry meaning: 400 invalid input, 401 not signed in, 403 not allowed, 404 not found, 409 conflicts with the current state (duplicate, invalid transition, still in use).

## Authentication

- Signing in sets a session cookie. The web application is served from the same origin as the API, so cookies work without tokens in browser storage or CORS.
- Unsafe requests (anything that changes data) must send the value of the `XSRF-TOKEN` cookie in the `X-XSRF-TOKEN` header. The token changes when the session changes, for example on sign-in, so clients refresh it and retry once on a 403.

## Access

- Public: reading published Movements and their revisions, registering, and signing in.
- Signed-in users (practitioners): managing their own Routines. Another user's Routine looks like it does not exist, so its existence is not revealed.
- Maintainers: everything that creates or changes Movements, drafts, Illustrations, Avatars, and Viewpoints, and reading drafts and generation requests.
- Access is enforced on the backend for every request, independent of the UI.

## Rules

- A Movement has at most one open draft. Revising requires a published revision and no open draft.
- Published revisions cannot be changed through the API.
- A Routine can only reference existing published revisions.
- Generation is asynchronous. A request returns immediately and its status is polled. It fails if its draft is no longer the open draft. A successful request replaces the draft's whole sequence.
- Pictures are uploaded separately, and a generation spec refers to the returned address.
- An Avatar or Viewpoint that a generation request uses cannot be deleted.
