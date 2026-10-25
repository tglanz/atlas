# Architecture

The application separates domain rules, use cases and transport or persistence adapters. Dependency checks keep those boundaries enforceable as rendering and storage change.

## Domain and use cases

Domain code is framework independent. It owns validity rules and state transitions, while application services orchestrate operations through ports. Persistence representations stay behind outbound adapters so storage choices do not become domain requirements.

Features communicate through application ports and explicit outbound adapters. A feature must not read another feature's persistence or call its implementation services. Movement knowledge remains independent from routines; routine references resolve through the movement feature's public use cases.

## HTML and API transports

The web composition layer builds page models from public use cases across features. It may compose domain results, but it must not access persistence adapters or move product rules into controllers or templates. This allows pages to combine movement, graph and generation information without introducing dependencies between those features.

HTML forms and JSON requests have separate binding and response concerns while reusing the same domain behavior. HTML renders validation errors with the submitted draft; API errors use problem details. Missing resources and authorization failures retain meaningful HTTP responses rather than appearing as successful content pages.

A Save that changes movement content and relationships must commit both together. Cross-feature page orchestration therefore has a transactional service boundary, and the controller redisplays a failure only after that transaction has rolled back. Illustration generation and approval remain separate from the editor draft so they cannot silently publish unsaved changes.

## Authorization and rendering

Spring Security enforces route access, and application services enforce ownership. Authentication details stay at the transport boundary; use cases receive the user identity needed for the operation.

Thymeleaf escapes authored text by default. Metadata uses a trusted public origin, and structured data must remain safe inside its HTML script element. Page composition must preserve the confidentiality of private responses and session-dependent content when setting caching behavior.

## Background work

Slow image-provider calls run outside database transactions. Short transactions record progress and apply reviewed results, avoiding locks while waiting on an external service. A restarted process cannot resume work that existed only in memory, so interrupted runs must be resolved deliberately.

See the [generation decision](../docs/rfcs/image-generation.md), [rendering decisions](../docs/stack.md), and [tooling](../docs/tooling.md). The architecture tests are the executable source of truth for dependency rules.
