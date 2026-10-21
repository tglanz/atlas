# Progress

Status as of 2026-10-21.

## Where we are

- **Docs**: product, domain, flows, design spec, API, tooling and backend architecture are written.
- **Backend `atlas_backend`**: runs end to end on SQLite (verified by a manual HTTP walkthrough). All planned backend features exist; storage and generation are stubs.
- **Frontend `atlas_ui`**: feature-based structure with an API client, sign in and register, and pages wired to the backend: movement library, details, revision history and editor (including Illustration upload and generation), routines, Avatar and Viewpoint settings, profile figure style. Checked by build, lint and a dev-server proxy walkthrough; never loaded in a browser.
- **CI**: builds and tests both projects on push and pull request.
- **UI hosting**: the backend serves the built UI from the same origin (one jar), so cookies and CSRF work without CORS.
- **Dev container**: written, never built.

### Backend features

- **Movement**: draft, publish and revise lifecycle; immutable published revisions; cues, tags, links; search by name and tag; revision history. Viewers get the Illustrations of one Avatar (preferred, else default).
- **Avatar**: CRUD, one default Avatar, specification visible to Maintainers only.
- **Generation**: Viewpoint presets, generation requests with stages (words or reference picture), background execution, retry, picture upload. Stubbed generator and storage return a placeholder URL.
- **Routine**: private to the owner, sections, pinned published Movement revisions.
- **Account**: registration, session login and logout, roles (Practitioner, Maintainer; the first user is a Maintainer), preferred Avatar.
- **Quality gates**: Spotless, javac lint, ArchUnit layering rules, 23 tests.

## What is missing

### Backend

- Real S3 storage and a real illustration generator.
- Recovery of generation requests left Running by a restart, and cleanup of old requests.
- Account management: password reset, email verification, login throttling, change email or password.
- Movement: discarding a draft, validating link targets, a tag listing endpoint for filters, pagination, removing or reordering a single Illustration (only the whole Avatar sequence can be cleared).
- Routine details return references only, not Movement names.
- CORS configuration. There is none.
- Deployment: Dockerfile, Render configuration, PostgreSQL profile verified against a real database.
- Persistent sessions. They are in memory.

### Frontend

- Checking the pages in a browser. They compile and the API calls work through the dev proxy, but no one has used them yet.
- "Add to routine" from a Movement page, and name previews for linked Movements.
- Pagination, and a tag picker for filtering.
- Component tests. Only the API client has tests.

### Infrastructure

- Backend hosting, database hosting, secrets.

## What is bad

1. **No CORS or cross-site cookies.** This is fine because the UI is served by the backend on the same origin, but a UI hosted elsewhere would not be able to log in.
2. **The first registered user becomes Maintainer.** On a fresh deployment, whoever registers first gets full control. On PostgreSQL, two simultaneous first registrations could both become Maintainers.
3. **Registration is open with no verification.** Spam accounts are possible, and the duplicate-email response reveals which emails exist.
4. **Heavy structure for a proof of concept.** Each feature has use-case interfaces, repository ports, separate JPA entities and mappers. The cost is mostly boilerplate. A leaner shape was proposed and is undecided.
5. **Tests are thin by choice.** API behavior (authorization, publish flow, generation flow, session and CSRF handling) has no automated tests. `AGENTS.md` asks for tests on changed behavior, which conflicts with the decision to cut them.
6. **Generation scheduler is in the wrong package.** `TaskExecutorGenerationScheduler` is an outbound adapter that triggers an inbound use case. It works but misplaces a background job.
7. **Illustration stores a URL, not a storage key.** With S3 (especially private buckets or signed URLs) a key plus a base URL is likely a better fit. Changing it later touches the schema.
8. **SQLite runs with a single connection.** It is fine for a proof of concept and not for production load.
9. **Adding an Avatar does not reach old Movements.** Illustrations are frozen with each revision, so existing published revisions fall back to the default Avatar until new revisions are published. This is intentional, but there is no tool to make it easy.
10. **Some tooling gaps.** `build.gradle` has no formatter (the Eclipse-based one could not download). The dev container is untested.
11. **Design spec lags the model.** It has no profile settings page, no Avatar choice UI, and no generation or upload UI, which now exist in the UI.

## Next steps

1. Decide whether to keep the current architecture or move to the leaner shape.
2. Replace the first-user rule with an explicit bootstrap, such as a Maintainer seeded from an environment variable.
3. Update `design-spec.md` for profile settings, Avatar choice, generation and upload.
4. Use the UI in a browser against the backend and fix what is rough.
5. Add "Add to routine" on the Movement page.
6. Deploy the backend (one jar with the UI) and database, and run the migrations on PostgreSQL.
7. Implement the real S3 storage and the real generator behind the existing ports.
8. Harden accounts: email verification, password reset, throttling.
9. Add a small set of API tests for authorization and the publish flow, and recover stuck generation requests.
