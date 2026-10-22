# Agent Instructions

## General Principles

- Be concise and direct.
- Avoid filler, emojis, and decorative formatting.
- Prefer simple, maintainable solutions over unnecessary abstractions.
- Do not invent requirements or make assumptions when ambiguity materially affects the design.
- Inspect the existing codebase before making changes.
- Follow established project conventions.
- Do not claim actions succeeded unless verified.

## Product Design

- Treat `./docs` as the source of truth for product requirements, domain concepts, user flows, and technical decisions.
- Read the relevant documentation before implementing features.
- Preserve documented domain invariants.
- If implementation conflicts with documentation, identify the conflict before proceeding.
- Update relevant documentation when agreed decisions change.
- Keep domain specifications free of implementation commentary.

## Documentation

Docs record what stays true as the code changes. The code, migrations, and configuration are the source of truth for everything else.

- Put in docs: goals, domain concepts and invariants, decisions and the reasons for them, and commands that rarely change.
- Keep out of docs: lists of files, classes, endpoints, fields, or tables; ids, names, or contents of seeded data; limits and other constants; counts; versions (except in the tooling doc); and snapshots of current status or behavior that go stale.
- Do not describe what the code already shows. Explain why, not what.
- READMEs show the commands to build, test, format, and run, and point to deeper docs. They are not state descriptions.
- Prefer a pointer to the code, or to a reference generated from it, over copying details into prose.
- When a change would make a doc stale, remove the volatile detail instead of updating it.
- When in doubt, leave it out.

## UI and UX

- Prioritize clarity, consistency, accessibility, and usability.
- Prefer cohesive user workflows over fragmented pages.
- Use dedicated pages for substantial views and workflows.
- Use tabs for closely related content and modals for short contextual actions.
- Avoid unnecessary complexity and visual clutter.
- Provide clear loading, empty, error, and permission states.
- Support keyboard navigation and visible focus states.
- Maintain consistent typography, spacing, and component behavior.

## Implementation

- Prefer small, focused changes.
- Separate presentation, domain logic, and data access appropriately.
- Avoid unnecessary dependencies and abstractions.
- Keep types explicit at system boundaries.
- Validate inputs and handle failures deliberately.
- Keep secrets out of source control.
- Enforce authorization on the backend.
- Add or update tests for changed behavior.
- Run relevant checks when possible.
- Report checks that could not be run.

## Communication

- Be terse and precise.
- Avoid repeating information.
- Explain tradeoffs when they materially affect a decision.
- Challenge assumptions when there is a concrete reason.
- Do not ask questions when the work can reasonably proceed without them.
- Do not claim files, tests, or deployments were changed or completed without verification.