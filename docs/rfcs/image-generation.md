# RFC: Image generation

Status: Draft

## Summary

Generate the Illustrations of a Movement with an external provider. Generated images are first shown to the maintainer as IllustrationCandidates, and only an explicit approval puts them on the Movement. The first provider is fal.ai, behind a boundary that lets other providers be added. fal.ai's behavior is documented in `docs/specs/falai.md`.

## Glossary

- **Movement**: an exercise or skill in the library.
- **Illustration**: an image created for a Movement and shown on it. A Movement has an ordered list of them. Each is an image URL.
- **Image**: any image file, whatever it is for. A generated image is just an image while it is an IllustrationCandidate, and becomes an Illustration only when approved onto a Movement.
- **Avatar**: the look generated images should have, as a text specification.
- **Viewpoint**: the viewing angle, as a text specification.
- **GenerationMovementIllustrations**: the generation state for a Movement's Illustrations. A record in our database that stands for a maintainer's order to generate one image per IllustrationStage for a Movement. It holds the IllustrationStages and tracks progress, so the UI can poll it while a worker generates in the background.
- **GenerationMovementIllustrationsRequest**: the body the UI sends to the API to start a generation. It is the API request, not the record.
- **GenerationMovementIllustrationsResponse**: what the API returns for a GenerationMovementIllustrations.
- **IllustrationStage**: one image to generate, defined by an Avatar, a Viewpoint, and free-text instructions. It belongs to one GenerationMovementIllustrations and has a position in it.
- **IllustrationCandidate**: the image generated for an IllustrationStage, with its state and provider details. It is not on the Movement until approved.
- **Provider**: the external service that generates images, such as fal.ai.
- **Provider request**: one call to the provider, identified by the provider's request id. An IllustrationStage makes one.
- **Connector**: our code that talks to one provider.
- **Image URL**: the web URL where an image can be loaded.
- **Review**: the maintainer looking at IllustrationCandidates next to the Movement's current Illustrations.
- **Approve**: putting a GenerationMovementIllustrations's IllustrationCandidates on the Movement, replacing its Illustrations.
- **Maintainer**: a user allowed to edit Movements.

## Goals and requirements

Goal: a maintainer can get generated Illustrations onto a Movement, checking them first, with the provider replaceable.

Out of scope: images a maintainer provides themselves (they are not generation), and the cost of generation (it is part of the platform subscription).

### P0: the first implementation

- Generate one image per IllustrationStage. Its system prompt is its own Avatar's and Viewpoint's specifications, and its image prompt is the Movement's name and description plus the IllustrationStage's instructions.
- Generation only runs from an explicit Generate action by a maintainer.
- A Movement has at most one generation in progress. Generate is disabled in the UI while a record of the Movement is `PENDING` or `RUNNING`, and the backend rejects it.
- It runs in the background, one IllustrationStage at a time. A GenerationMovementIllustrations has a status that the UI can poll.
- While it runs, the UI shows its IllustrationStages one by one: each IllustrationStage that is not finished shows a spinner, and its image appears as soon as it is `DONE`.
- IllustrationCandidates are reviewed before they change the Movement. Approving replaces the Movement's Illustrations with the IllustrationCandidates, in order. A maintainer can also discard.
- The first IllustrationStage that fails fails the whole GenerationMovementIllustrations, with a reason, and the worker stops. IllustrationStages after it are not attempted. The maintainer starts a new generation. There is no retry.
- Images stay where the provider put them, and we keep their image URLs. Every call sends the file-lifetime header set to no expiry, so approved Illustrations do not expire at the provider's default.
- Each IllustrationCandidate records the provider, the model, the provider's request id, and when it was obtained. The provider request id is stored as soon as the job is submitted, not when it finishes.
- Providers sit behind one boundary, chosen by configuration. fal.ai is the first connector. A stub stays for development and tests.
- The provider key comes from the environment (`ATLAS_FAL_AI_API_KEY`) and is never logged or returned.
- Only maintainers can generate, review, approve or discard.

### P1: can wait

- A reference image per IllustrationStage. It needs uploaded images the provider can reach, so storage for uploads, and the provider's image-edit model.
- Retry a failed GenerationMovementIllustrations, redoing the IllustrationStages that are not `DONE` and keeping the finished ones.
- Retry transient provider errors, such as timeouts and rate limits, inside the connector.
- Copy approved images into storage we control.
- Regenerate a single IllustrationStage while reviewing.
- Keep the figure consistent across IllustrationStages, by giving each the previous one's image as its reference.
- Recover an IllustrationStage after a timeout or restart by asking the provider about the stored provider request id, instead of submitting again.
- A periodic check that images are still reachable, with unreachable ones shown to maintainers.
- A GenerationMovementIllustrations left unreviewed for too long expires.

### P2: questionable for now

- Webhooks from the provider instead of polling.
- Running IllustrationStages in parallel.
- Approving some IllustrationStages and regenerating others.
- Editing the prompt template from the UI.
- Choosing a provider or model per GenerationMovementIllustrations.

### What fal.ai constrains

- One provider request makes one image from one image prompt, plus an optional system prompt. Several different images need several provider requests, so each IllustrationStage makes its own.
- Provider requests go through fal.ai's queue: submit, poll the returned status URL, fetch the returned result URL. The key goes in an `Authorization: Key` header.
- The queue keeps its result for only about an hour, so a finished result must be fetched promptly. The image file itself lives on fal.ai's CDN.
- A new account runs only two provider requests at once; others wait. One IllustrationStage at a time stays within that.
- The image size, ratio and format are connector settings, not part of the domain.
- Documentation conflicts on file lifetime: one page says files last at least seven days, another says no expiry unless configured. Files are public by default, which is fine for public Movement content.
- A file's lifetime is fixed when it is created and cannot be extended. We cannot know which IllustrationCandidates will be approved, so every provider request asks for the same lifetime.

Decision: no expiry on every provider request, by setting the lifetime header's expiration to null, so it needs no account-level permission. Why: it is the only value that cannot break an approved Illustration. The cost is that IllustrationCandidates that are never approved also stay at the provider. That is accepted for now, and the provider's delete operation can clear them later. Copying approved images into our own storage (P1) is about independence from the provider, not a deadline. Before relying on this, confirm on the first real generation that the header accepts null, and that the file is still reachable after the documented minimum.

## Data model

### Datastores

- **Our relational database** (SQLite now, PostgreSQL later): all the tables below.
- **fal.ai CDN**: the image files. We keep only their image URLs.
- **Our object storage** (S3-compatible, P1): copies of approved images, and uploaded images. Not part of P0.

### Tables

New or changed, in the relational database. Existing tables that do not change are not repeated.

- **`generation_movement_illustrations_request`** (replaces `generation_request`): one row per GenerationMovementIllustrations.
  - `id`: uuid, primary key
  - `movement_key`: text, references `movement`
  - `status`: text, one of `PENDING`, `RUNNING`, `READY_FOR_REVIEW`, `APPLIED`, `DISCARDED`, `FAILED`
  - `failure_reason`: text, null unless `FAILED`
  - `created_at`: timestamp
  - `finished_at`: timestamp, null until the status is `READY_FOR_REVIEW`, `APPLIED`, `DISCARDED` or `FAILED`
  - unique index on `movement_key` limited to rows whose status is `PENDING` or `RUNNING`, so the database itself allows at most one generation in progress per Movement
- **`generation_illustration_stage`** (replaces `generation_stage` and `generation_result`): one row per IllustrationStage, holding its definition and its IllustrationCandidate.
  - `request_id`: uuid, references `generation_movement_illustrations_request`, part of the primary key
  - `position`: integer, part of the primary key
  - `avatar_id`: uuid, references `avatar`
  - `viewpoint_id`: uuid, references `viewpoint`
  - `instructions`: text
  - `candidate_state`: text, one of `PENDING`, `DONE`, `FAILED`
  - `candidate_image_url`: text, null until `DONE`
  - `candidate_failure_reason`: text, null unless `FAILED`
  - `provider`: text, null until submitted
  - `model`: text, null until submitted
  - `provider_request_id`: text, null until submitted
  - `provider_status_url`: text, null until submitted
  - `provider_result_url`: text, null until submitted
  - `obtained_at`: timestamp, null until `DONE`
- **`movement_illustration`** (existing): the Movement's ordered Illustrations.
  - `movement_key`, `position`, `image_url` (renamed from `url`)

### Domain objects

- **GenerationMovementIllustrations**: `id`, `movementKey`, `status`, `failureReason`, `createdAt`, `finishedAt`, `stages` (a list of IllustrationStage ordered by position).
- **IllustrationStage**: `position`, `avatarId`, `viewpointId`, `instructions`, `candidate` (an IllustrationCandidate).
- **IllustrationCandidate**: `state`, `imageUrl`, `failureReason`, `provider`, `model`, `providerRequestId`, `providerStatusUrl`, `providerResultUrl`, `obtainedAt`.
- **Illustration** (existing): `imageUrl`, in the Movement's ordered list.
- **Avatar** and **Viewpoint** (existing): `id`, `name`, `specification`.

### Relations

- A GenerationMovementIllustrations belongs to one Movement and has one or more IllustrationStages.
- An IllustrationStage has one Avatar, one Viewpoint and one IllustrationCandidate.
- Approving copies each IllustrationCandidate's `imageUrl`, in position order, into the Movement's Illustrations.

### Status of a GenerationMovementIllustrations

- `PENDING` to `RUNNING`, when the worker takes it
- `RUNNING` to `READY_FOR_REVIEW`, when every IllustrationCandidate is `DONE`
- `RUNNING` to `FAILED`, when an IllustrationStage fails for good, which stops the worker
- `READY_FOR_REVIEW` to `APPLIED`, on approval
- `READY_FOR_REVIEW` to `DISCARDED`, on discard

## API

All endpoints are maintainer-only. `{key}` is the Movement's key and `{id}` the GenerationMovementIllustrations's id. Provider details (request id, URLs, model) are never returned to the UI.

### Objects

- **GenerationMovementIllustrationsRequest**
  - `stages`: a list of IllustrationStageInput, at least one
- **IllustrationStageInput**
  - `avatarId`: uuid
  - `viewpointId`: uuid
  - `instructions`: string, may be empty
- **GenerationMovementIllustrationsResponse**
  - `id`: uuid
  - `movementKey`: string
  - `status`: `PENDING`, `RUNNING`, `READY_FOR_REVIEW`, `APPLIED`, `DISCARDED` or `FAILED`
  - `failureReason`: string or null
  - `createdAt`: timestamp
  - `finishedAt`: timestamp or null
  - `stages`: a list of IllustrationStageResponse, by position
- **IllustrationStageResponse**
  - `position`: integer
  - `avatarId`: uuid
  - `viewpointId`: uuid
  - `instructions`: string
  - `candidate`: IllustrationCandidateResponse
- **IllustrationCandidateResponse**
  - `state`: `PENDING`, `DONE` or `FAILED`
  - `imageUrl`: string or null
  - `failureReason`: string or null
- **MovementResponse** (existing)
  - `key`, `name`, `description`, `items`, `tags`, and `illustrations`: a list of image URLs

### Endpoints

- `POST /api/movements/{key}/generations`
  - Body: GenerationMovementIllustrationsRequest
  - Returns 202 with a GenerationMovementIllustrationsResponse, status `PENDING`
  - 400 if there are no stages or an Avatar or Viewpoint does not exist. 404 if the Movement does not exist.
  - 409 if the Movement already has a record whose status is `PENDING` or `RUNNING`. The check in the code gives a clear message, and the unique index catches two simultaneous calls.
  - Changed: the body no longer allows static images.
- `GET /api/movements/{key}/generations`
  - Returns 200 with a list of GenerationMovementIllustrationsResponse, newest first
- `GET /api/movements/{key}/generations/{id}`
  - Returns 200 with a GenerationMovementIllustrationsResponse. The UI polls this.
  - 404 if it does not exist for that Movement.
  - Changed: `candidate` replaces the former results list.
- `POST /api/movements/{key}/generations/{id}/apply` (new)
  - No body. Replaces the Movement's Illustrations with the IllustrationCandidates' image URLs in position order.
  - Returns 200 with a MovementResponse.
  - 409 unless the status is `READY_FOR_REVIEW`. 404 if it does not exist.
- `POST /api/movements/{key}/generations/{id}/discard` (new)
  - No body. Sets the status to `DISCARDED`.
  - Returns 204. 409 unless the status is `READY_FOR_REVIEW`. 404 if it does not exist.

Saving a Movement never changes its Illustrations. Only apply does.

## Core flows

### Generate and approve

1. In the Movement editor the maintainer enters IllustrationStages, each with an Avatar, a Viewpoint and instructions, and presses Generate.
2. The UI calls the create endpoint. The backend checks that the Movement, Avatars and Viewpoints exist, that there is at least one IllustrationStage, and that no other record of the Movement is `PENDING` or `RUNNING`. It stores a GenerationMovementIllustrations as `PENDING`, with every IllustrationCandidate `PENDING`, and returns it.
3. The UI polls the read endpoint while the status is `PENDING` or `RUNNING`. Generate stays disabled for that Movement meanwhile. Each IllustrationStage whose candidate is `PENDING` shows a spinner. When a poll returns a candidate as `DONE`, its spinner is replaced by the image, loaded from its `candidate_image_url`, before the other IllustrationStages have finished.
4. A background worker takes the GenerationMovementIllustrations and sets it `RUNNING`.
5. For each IllustrationStage, in position order:
   1. Compose the system prompt from its Avatar's and Viewpoint's specifications, and the image prompt from the Movement's name and description and its instructions.
   2. The connector submits one provider request to fal.ai's queue, asking for one image with the lifetime header set to no expiry. It stores `provider`, `model`, `provider_request_id` and the returned status and result URLs on the row at once.
   3. It polls the status URL until the provider request completes, within a time limit.
   4. It fetches the result right away and reads the image URL of the first image.
   5. It sets `candidate_state` to `DONE`, stores `candidate_image_url`, and sets `obtained_at`.
6. When every IllustrationCandidate is `DONE`, the status becomes `READY_FOR_REVIEW` and `finished_at` is set.
7. The UI shows all the IllustrationCandidates in order, next to the Movement's current Illustrations, and enables Approve and Discard. Until then they are not available.
8. The maintainer approves. The UI calls the apply endpoint.
9. The backend checks the status is `READY_FOR_REVIEW` and the Movement still exists. In one transaction it replaces the Movement's `movement_illustration` rows with the image URLs in position order and sets the status to `APPLIED`.
10. The UI reloads the Movement.

### An IllustrationStage fails

1. During step 5 a provider request fails.
2. The connector does not retry. The IllustrationCandidate's state becomes `FAILED` with a `candidate_failure_reason`, such as the provider refusing the content.
3. The worker stops. IllustrationStages after the failed one stay `PENDING`, and finished IllustrationCandidates are kept.
4. The status becomes `FAILED` with a `failure_reason`, and the UI shows which IllustrationStage failed and why, and the images that did succeed.
5. The maintainer can start a new generation. A `FAILED` record does not block it, and stays as history.

### Discard

1. A GenerationMovementIllustrations is `READY_FOR_REVIEW` and the maintainer does not want the result.
2. The UI calls the discard endpoint. The status becomes `DISCARDED` and the Movement is unchanged.
3. The maintainer can create a new GenerationMovementIllustrations, with changed instructions for example.

## Changes to current state

- **Approval is required.** A successful generation no longer replaces the Movement's Illustrations. The `SUCCEEDED` status is replaced by `READY_FOR_REVIEW`, `APPLIED` and `DISCARDED`.
- **Generation starts from a Generate action**, not from Save. Save never touches Illustrations.
- **Static stages leave generation.** The spec and the editor's generation form lose them. Setting Illustrations from the maintainer's own images becomes a separate editor section that sets them directly, with no GenerationMovementIllustrations and no review. It is outside this RFC.
- **Prompts are built per IllustrationStage** from that IllustrationStage's own Avatar and Viewpoint.
- **Tables:** `generation_request` becomes `generation_movement_illustrations_request`. `generation_stage` and `generation_result` merge into `generation_illustration_stage`, which also stores the provider details.
- **API:** apply and discard are added, the create body loses static stages, and results become candidates.
- **One generation at a time per Movement.** The create endpoint returns 409 while another is `PENDING` or `RUNNING`, enforced by the code and by a unique index in the database.
- **Failure stops the generation.** The first IllustrationStage that fails fails the GenerationMovementIllustrations and stops the worker, so nothing further is spent on it.
- **No retry for now.** The retry endpoint is removed. Retrying a failed generation, and retrying transient provider errors, are P1.
- **Naming:** the record is `GenerationMovementIllustrations` in the code, where it is `GenerationRequest` today. `GenerationMovementIllustrationsRequest` names only the create endpoint's body, where the code has `GenerateRequest` today.
- **Terminology:** an Illustration is specific to a Movement, and an image is general. Columns and fields use `image_url`, not picture or address. In the code, `Picture`, `PictureStorage` and `PictureRules` become `Image`, `ImageStorage` and `ImageRules`.
