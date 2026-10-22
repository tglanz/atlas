# Progress

Last reviewed 2026-10-22.

## Where we are

A proof of concept. Movements, revisions, illustrations (uploaded or generated), routines, and accounts work end to end in the backend and are wired into the web app. Picture storage and illustration generation are stubs. The web app has been built and checked against the API but not used in a browser.

## Missing

- Real picture storage and a real illustration generator.
- Account management: password reset, email verification, login throttling.
- Recovery and cleanup of generation requests interrupted by a restart.
- Movement: discarding a draft, validating link targets, pagination, removing a single Illustration.
- "Add to routine" from a Movement page.
- Deployment: container image, hosting, a verified PostgreSQL setup.

## Known problems

- **First maintainer.** The first registered user becomes a Maintainer. On a fresh deployment whoever registers first gets full control, and simultaneous first registrations on PostgreSQL could both succeed.
- **Open registration** without verification invites spam accounts, and reveals which emails exist.
- **Same origin only.** The API has no CORS or cross-site cookie support, so the web app must be served by the backend.
- **Illustrations are stored as addresses, not storage keys.** Private buckets or signed addresses will likely need keys instead, which changes the schema.
- **Heavy structure for a proof of concept.** Ports, adapters and mappers in every feature add boilerplate. Whether to simplify is undecided.
- **Thin automated tests, by choice.** API behavior is not covered, although `AGENTS.md` asks for tests on changed behavior.
- **Single-connection SQLite** is fine locally and not for production load.

## Next steps

1. Decide whether to keep the current structure or simplify it.
2. Replace the first-user rule with an explicit bootstrap, such as a Maintainer configured from the environment.
3. Use the web app in a browser and fix what is rough.
4. Deploy the backend and database.
5. Implement real storage and generation behind the existing ports.
6. Harden accounts.
7. Add a small set of API tests for authorization and publishing.
