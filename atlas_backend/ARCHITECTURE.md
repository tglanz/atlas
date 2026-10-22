# Architecture

The backend follows hexagonal architecture, organized by feature. Each feature (`movement`, `avatar`, `generation`, `routine`, `account`) contains the same layers.

```
com.tglanz.atlas.backend
  <feature>/
    domain/               Entities, value objects, invariants. Plain Java.
    application/
      port/in/            Use-case interfaces called by inbound adapters.
      port/out/           Interfaces the application needs from the outside.
      service/            Use-case implementations. Transaction boundary.
    adapter/
      in/web/             REST controllers, request and response DTOs.
      out/persistence/    JPA entities, Spring Data repositories, port implementations.
  shared/                 Cross-feature kernel: error types, persistence helpers.
  config/                 Security, clock, HTTP error mapping.
```

## Rules

1. Dependencies point inward: adapters depend on the application layer, the application layer on the domain, the domain on nothing but the standard library and `shared`.
2. Domain classes enforce domain invariants (input validity, state transitions). Services orchestrate and do not hold rules.
3. Ports are owned by the application layer. Adapters implement outbound ports and call inbound ports.
4. JPA entities exist only in `adapter/out/persistence` and are mapped to domain objects. The domain has no framework annotations.
5. Transactions are declared on application services.
6. Inbound adapters never use outbound adapters.
7. Features do not depend on each other except through ports. `routine` reads Movement data only through `movement.application.port.in`, inside `routine.adapter.out.movement`. `movement` does not depend on `routine`.
8. Authorization is enforced by the backend: path rules in `config.SecurityConfig`, ownership checks in application services. Use cases receive only a user id; authentication details stay in `config` and `account.adapter`.

These rules are enforced by ArchUnit tests in `ArchitectureTest`.

## Features and dependencies

- `movement`: Movements and their ordered Illustrations.
- `avatar`: Avatars, the looks used to generate Illustrations.
- `generation`: Viewpoints, generation requests (a spec of stages, each avatar-based or a static image), and picture uploads.
- `routine`: Routines, which list Movements by key.
- `account`: users and roles.

```
generation -> movement, avatar
routine    -> movement
```

`movement`, `avatar` and `account` depend on no other feature. Each arrow is an outbound adapter in the dependent feature, such as `generation.adapter.out.movement`. `ArchitectureTest` rejects any other dependency between features.

## Generation

A generation request runs in the background on the application task executor. The run is split into short transactions around the slow generator call, so no database transaction stays open while waiting on the third party:

1. `GenerationLifecycle.begin` marks the request running and resolves each stage into a step: use a static image as is, or generate a picture.
2. The generator and picture storage are called outside any transaction, once per generated stage.
3. `GenerationLifecycle.complete` replaces the Movement's Illustrations with one per stage, or `fail` records the reason.

`IllustrationGenerator` and `PictureStorage` are ports. Their current adapters are stubs; the storage target is S3.

## Errors

- `NotFoundException`: 404
- `ConflictException`: 409 (invalid state transition, duplicate, still referenced)
- `IllegalArgumentException` and request validation failures: 400

Errors are returned as RFC 9457 problem details.

## Persistence

- SQLite by default; PostgreSQL through the `postgres` profile.
- Flyway migrations are plain, portable SQL.
- Identifiers are stored as `VARCHAR(36)`; timestamps as `TIMESTAMP`.

## Stack

See `../docs/stack.md` for the technology choices and `../docs/tooling.md` for versions and commands.
