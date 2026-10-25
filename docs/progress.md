# Progress

Last reviewed 2026-10-25.

## Where we are

A proof of concept. Movements, their stages, illustration generation with review, routines, and accounts work end to end in the backend and are wired into the web app. Generation runs against a stub or fal.ai; the fal.ai connector has had little real use. The web app has been built and checked against the API but not used in a browser.

## Missing

- Our own image storage, retry of failed generations, and reference images for generation.
- Setting a Movement's Illustrations from the maintainer's own images.
- Account management: password reset, email verification, login throttling.
- Movement: deleting a Movement, pagination, removing a single Illustration.
- Using the graph: traversal exists in the API but nothing in the UI uses it yet, such as a progression ladder or paths between movements.
- Change history for Movements, and a way to keep a Movement hidden while it is being built. Both were deferred on purpose; see `technicalities.md`.
- "Add to routine" from a Movement page.
- Deployment: container image, hosting, a verified PostgreSQL setup.

## Known problems

- **First maintainer.** The first registered user becomes a Maintainer. On a fresh deployment whoever registers first gets full control, and simultaneous first registrations on PostgreSQL could both succeed.
- **Open registration** without verification invites spam accounts, and reveals which emails exist.
- **Same origin only.** The API has no CORS or cross-site cookie support, so the web app must be served by the backend.
- **Illustrations are stored as URLs, not storage keys.** Private buckets or signed URLs will likely need keys instead, which changes the schema.
- **Heavy structure for a proof of concept.** Ports, adapters and mappers in every feature add boilerplate. Whether to simplify is undecided.
- **Thin automated tests, by choice.** API behavior is not covered, although `AGENTS.md` asks for tests on changed behavior.
- **Single-connection SQLite** is fine locally and not for production load.

## Next steps

1. Decide whether to keep the current structure or simplify it.
2. Remove the first-user rule now that a Maintainer can be configured from the environment.
3. Use the web app in a browser and fix what is rough.
4. Deploy the backend and database.
5. Validate generation against the real provider, then add our own image storage.
6. Harden accounts.
7. Add a small set of API tests for authorization and movement editing.
