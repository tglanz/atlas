# API

Base path `/api`. JSON bodies. Errors are problem details.

## Access

| Role | Meaning |
| --- | --- |
| Public | No authentication |
| Authenticated | Any signed-in user (practitioner) |
| Maintainer | Signed-in user with the Maintainer role. The first registered user is a Maintainer. |

## Authentication

Session cookie based. Unsafe requests (POST, PUT, DELETE) must send the `XSRF-TOKEN` cookie value in the `X-XSRF-TOKEN` header.

| Method and path | Access | Purpose |
| --- | --- | --- |
| `POST /auth/register` | Public | JSON `email`, `password` (8 to 72 characters). Returns the user. |
| `POST /auth/login` | Public | Form fields `email`, `password`. Returns 204 and sets the session cookie, or 401. |
| `POST /auth/logout` | Authenticated | Ends the session. Returns 204. |
| `GET /auth/me` | Authenticated | Current user: `id`, `email`, `role`. |

## Movements

| Method and path | Access | Purpose |
| --- | --- | --- |
| `GET /movements?text=&tag=` | Public | Current published Movements, filtered by name text and tag |
| `GET /movements/{key}` | Public | Current published revision |
| `GET /movements/{key}/revisions` | Public | Published revisions |
| `GET /movements/{key}/revisions/{revision}` | Public | One published revision |
| `GET /movements/{key}/draft` | Maintainer | The open draft |
| `POST /movements` | Maintainer | Create a draft at revision 1. Body: `key`, `content` |
| `PUT /movements/{key}/draft` | Maintainer | Replace the draft content |
| `POST /movements/{key}/revisions` | Maintainer | Create a draft from the latest published revision |
| `POST /movements/{key}/draft/publish` | Maintainer | Publish the draft |

Movement content: `name`, `description`, `cues[]`, `tags[]`, `links[]` (`target`, `relationType`).

Responses carry `illustrations`: the ordered urls of the revision's Illustrations. In the list, `representativeIllustration` is the first one.

Draft Illustrations (Maintainer):

| Method and path | Purpose |
| --- | --- |
| `POST /media` | Upload a picture (multipart `file`; PNG, JPEG or WebP up to 10 MB). Returns its `url`, to use as an image in a spec. |
| `POST /movements/{key}/draft/generations` | Request production of the draft's Illustration sequence from a spec. Returns 202 with the request. |
| `GET /movements/{key}/draft/generations` | Generation requests, newest first |
| `GET /movements/{key}/draft/generations/{id}` | One request: `status`, `failureReason`, `results[]` |
| `POST /movements/{key}/draft/generations/{id}/retry` | Run a failed request again |
| `DELETE /movements/{key}/draft/illustrations` | Remove all Illustrations from the draft |

The generation body is `{ "stages": [ ... ] }`, 1 to 20 stages. Each stage sets exactly one of:

- `avatar`: `avatarId`, `viewpointId`, and `instructions` and/or `referenceImageUrl`.
- `staticImage`: `imageUrl`.

Stages of both kinds may be mixed in one request. One Illustration results per stage, and a successful run replaces the draft's sequence. A request fails if the draft it was made for is no longer open. Generation runs in the background. Storage and generation are currently stubs: every stored or generated picture gets the url `atlas.storage.stub-url`.

Rules:
- A key is lowercase letters, digits and hyphens.
- Tags and relation types are stored lowercase.
- A Movement has at most one draft.
- Creating a revision requires a published revision and no open draft.

## Avatars and Viewpoints

Maintainer only, including reads. `/avatars` and `/viewpoints` have the same shape.

- `GET /avatars`, `GET /avatars/{id}`
- `POST /avatars`, `PUT /avatars/{id}`, `DELETE /avatars/{id}`

Body: `name` (unique), `specification`. An Avatar or Viewpoint used by a generation request cannot be deleted (409).

## Routines

All routine endpoints require authentication and act on the caller's own Routines. Other users' Routines return 404.

| Method and path | Purpose |
| --- | --- |
| `GET /routines` | The caller's Routines |
| `GET /routines/{id}` | One Routine with sections and movement references |
| `POST /routines` | Create |
| `PUT /routines/{id}` | Replace name, description and sections |
| `DELETE /routines/{id}` | Delete |

Body: `name`, `description`, `sections[]` of `name`, `description`, `movements[]` (`key`, `revision`). Every reference must be an existing published revision.

## Not yet available

Real picture storage and generation (both are stubs), and removing or reordering a single Illustration.
