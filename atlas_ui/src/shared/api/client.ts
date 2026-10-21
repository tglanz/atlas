import { ApiError } from './errors';

const API_URL = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '');
const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

function readCookie(name: string): string | undefined {
  const entry = document.cookie
    .split('; ')
    .find((c) => c.startsWith(`${name}=`));
  return entry ? decodeURIComponent(entry.slice(name.length + 1)) : undefined;
}

/** The backend sets the XSRF-TOKEN cookie on any response; fetch once if it is missing. */
async function csrfToken(): Promise<string | undefined> {
  if (!readCookie('XSRF-TOKEN')) {
    await fetch(`${API_URL}/api/auth/me`, { credentials: 'include' });
  }
  return readCookie('XSRF-TOKEN');
}

export type RequestOptions = {
  method?: string;
  /** Sent as JSON unless it is already FormData or URLSearchParams. */
  body?: unknown;
  signal?: AbortSignal;
};

/** The only place that calls fetch. Throws ApiError for non-2xx responses. */
export async function apiFetch<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const method = (options.method ?? 'GET').toUpperCase();
  const headers = new Headers({ Accept: 'application/json' });
  let body: BodyInit | undefined;

  if (
    options.body instanceof FormData ||
    options.body instanceof URLSearchParams
  ) {
    body = options.body;
  } else if (options.body !== undefined) {
    headers.set('Content-Type', 'application/json');
    body = JSON.stringify(options.body);
  }
  if (!SAFE_METHODS.has(method)) {
    const token = await csrfToken();
    if (token) headers.set('X-XSRF-TOKEN', token);
  }

  const response = await fetch(`${API_URL}/api${path}`, {
    method,
    headers,
    body,
    credentials: 'include',
    signal: options.signal,
  });

  if (!response.ok) {
    throw new ApiError(response.status, await problemDetail(response));
  }
  return (response.status === 204 ? undefined : await response.json()) as T;
}

async function problemDetail(response: Response): Promise<string> {
  try {
    const problem = (await response.json()) as { detail?: string };
    if (problem.detail) return problem.detail;
  } catch {
    // The body was empty or not JSON.
  }
  return response.statusText || `Request failed (${response.status})`;
}
