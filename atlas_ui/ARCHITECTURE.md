# Architecture

The UI is organized by feature. Each feature owns everything a part of the product needs, and the rest of the code reaches it through one public entry point.

```
src/
  main.tsx          entry point
  app/              wiring that knows about every feature
    App.tsx           providers: Mantine, TanStack Query, router
    router.tsx        every route, and which routes need a signed-in user or a maintainer
    layout/           app shell, header, navigation
    pages/            landing page, not-found page, pages that combine several features
    theme.ts
  shared/           code with no product knowledge
    api/              the only code that calls fetch; ApiError
    ui/               small reusable components
  features/
    auth/             sign in, register, current user, route guard, account menu
    movements/        library, details, editor
    avatars/          Avatar settings
    generation/       Viewpoint settings, setting a movement's Illustrations (generate or static images)
    routines/         routine library, details, editor
```

Inside a feature:

- `api.ts`: functions that call the backend, and the types of what it returns.
- `queries.ts`: TanStack Query hooks built on `api.ts`.
- `components/`: pieces used inside the feature.
- `pages/`: components that routes render.
- `index.ts`: the public entry point. It lists what other code may import.

## Rules

1. Server data lives in TanStack Query. It is never copied into Zustand or component state.
2. Zustand is for client-only state. Nothing needs it yet.
3. Filters and search live in the URL as search params.
4. `shared` imports nothing from `features` or `app`.
5. A feature imports other features only through their `index.ts`, never their internals, and never imports from `app`.
6. Dependencies between features point one way: `movements` uses `generation`, which uses `avatars`; `routines` uses `movements`. Features that need to combine two features that don't depend on each other are composed in `app/pages`.
7. Only `shared/api` calls `fetch`.
8. Route guards only shape the UI. The backend enforces access.

The import rules are checked by `oxlint` (see `.oxlintrc.json`).

## Data flow

- Pages call query hooks. `QueryBoundary` shows the loading and error states.
- Mutations call the backend and then invalidate the related queries.
- Who is signed in is a query on `/api/auth/me`. The session cookie is the source of truth.

## API client

- `apiFetch` adds the `/api` prefix, sends cookies, and sends the CSRF token from the `XSRF-TOKEN` cookie on unsafe requests.
- Non-2xx responses throw `ApiError` carrying the backend's problem detail text.
- In development, Vite proxies `/api` to the backend (`VITE_DEV_API`, default `http://localhost:8080`), so the UI and API share an origin and cookies work.
- In production the backend serves the built UI, so the API is always same-origin. `VITE_API_URL` exists for pointing a build at another origin, which would also need CORS and cross-site cookie support on the backend (not implemented).

## Conventions

- Forms use `@mantine/form`.
- Every screen handles loading, error and empty states.
- Pages stay simple: Mantine components, little custom styling.
- Tests are basic: logic that is easy to get wrong, not every component.
