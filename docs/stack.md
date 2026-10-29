# Architecture Decisions

## Render pages in the application

Spring MVC and Thymeleaf render pages from application use cases. Public movement content and metadata belong in the initial HTML so visitors and crawlers can read them without executing browser code. Reading database content at request time also lets maintainers publish changes without rebuilding a frontend or regenerating static pages.

The application owns page rendering and the JSON API in the same deployable. This keeps domain rules and authorization together and avoids operating another rendering service. The API remains a separate transport for utilities and other explicit clients; page rendering calls use cases directly rather than making HTTP requests back into the application.

## Enhance interactions where they help

Ordinary links and forms provide navigation and submission. Small browser scripts handle local editor drafts, searchable choices and targeted HTML updates where a full navigation would interrupt work. Templates share layout and UI fragments, while common styles preserve a consistent visual language.

Interactive state stays local until Save. Refreshing a generation panel must not replace unsaved movement inputs. Server validation remains authoritative, and unsuccessful submissions must preserve entered values.

## Share an origin and protect private state

Session cookies and CSRF protection stay within the application origin, avoiding a separate cross-origin authentication protocol. The server enforces authorization regardless of which controls a page displays.

HTML containing session-dependent navigation, private content or CSRF tokens must not be shared between users through caches. Caching and asset invalidation must preserve the visibility of published edits and deployment changes.

Public URLs in metadata come from a configured, trusted deployment origin. Search discovery depends on accurate content, crawlable links and correct HTTP behavior; rendering on the server alone does not establish indexing, rankings or faster loading.

See the [backend architecture](../atlas_backend/ARCHITECTURE.md) for dependency boundaries and [tooling](tooling.md) for build commands and runtime requirements.

## Discovery and shared previews

Movement pages expose their names, descriptions, cues and relationships in the initial HTML. Social metadata uses the first Illustration as its preview image, with an absolute URL based on the trusted deployment origin. Small cached JPEG derivatives keep shared previews inexpensive to fetch without altering the stored Illustration. Only trusted image hosts may supply derivatives; page rendering does not wait for image downloads. The sitemap lists the complete Movement catalog and its representative images; crawler rules allow public Movement discovery, including ChatGPT search.

Public Routine links are intentionally excluded from the sitemap and request no indexing. They support sharing a particular practice, while catalog discovery belongs to Movements. Link revocation and private editing are enforced by the application rather than crawler instructions.
