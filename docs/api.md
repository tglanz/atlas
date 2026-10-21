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
| `GET /auth/me` | Authenticated | Current user: `id`, `email`, `role`, `preferredAvatarId`. |
| `PUT /auth/me/avatar` | Authenticated | JSON `avatarId` (null clears). Sets the preferred Avatar. Unknown Avatar returns 400. |

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

Draft Illustrations (Maintainer):

| Method and path | Purpose |
| --- | --- |
| `POST /movements/{key}/draft/generations` | Request generation. Body: `avatarId`, `viewpointId`, `stages[]` (`description` and/or `referenceUrl`). Returns 202 with the request. |
| `GET /movements/{key}/draft/generations` | Generation requests, newest first |
| `GET /movements/{key}/draft/generations/{id}` | One request: `status`, `failureReason`, `results[]` |
| `POST /movements/{key}/draft/generations/{id}/retry` | Run a failed request again |
| `POST /movements/{key}/draft/avatars/{avatarId}/illustrations` | Upload a picture (multipart `file`; PNG, JPEG or WebP up to 10 MB). Appended to the Avatar's sequence. |
| `DELETE /movements/{key}/draft/avatars/{avatarId}/illustrations` | Remove the Avatar's whole sequence from the draft |

Generation runs in the background. A request fails if the draft it was made for is no longer open. Storage and generation are currently stubs: every stored or generated picture gets the url `atlas.storage.stub-url`.

Responses:
- Reads by viewers (`GET /movements`, `/movements/{key}`, `/movements/{key}/revisions/{revision}`) show the Illustrations of one Avatar: `avatarId` and an ordered `illustrations[]`. The Avatar is the signed-in user's preferred Avatar, or the default Avatar for visitors and when the preferred Avatar has no Illustrations in that revision. In the list, `representativeIllustration` is the first of them.
- Maintainer endpoints (`GET /movements/{key}/draft` and all writes) return `illustrations` grouped by Avatar id.

Rules:
- A key is lowercase letters, digits and hyphens.
- Tags and relation types are stored lowercase.
- A Movement has at most one draft.
- Creating a revision requires a published revision and no open draft.

## Avatars and Viewpoints

`/avatars` and `/viewpoints` have the same shape. Viewpoints are Maintainer-only, including reads.

| Method and path | Access |
| --- | --- |
| `GET /avatars`, `GET /avatars/{id}` | Public (specification returned to Maintainers only) |
| `POST /avatars`, `PUT /avatars/{id}`, `DELETE /avatars/{id}` | Maintainer |
| `PUT /avatars/{id}/default` | Maintainer |

Body: `name` (unique), `specification`. Avatars also return `isDefault`. The first Avatar created is the default; `PUT /avatars/{id}/default` moves it. The default Avatar cannot be deleted, and an Avatar with Movement illustrations cannot be deleted (409).

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

Illustration generation and upload, and illustration write endpoints.
