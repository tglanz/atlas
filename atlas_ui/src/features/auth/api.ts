import { apiFetch, isApiError } from '@/shared/api';

export type Role = 'PRACTITIONER' | 'MAINTAINER';

export type CurrentUser = {
  id: string;
  email: string;
  role: Role;
};

/** The signed-in user, or null for a visitor. */
export async function fetchCurrentUser(): Promise<CurrentUser | null> {
  try {
    return await apiFetch<CurrentUser>('/auth/me');
  } catch (error) {
    if (isApiError(error, 401)) return null;
    throw error;
  }
}

export function register(
  email: string,
  password: string,
): Promise<CurrentUser> {
  return apiFetch<CurrentUser>('/auth/register', {
    method: 'POST',
    body: { email, password },
  });
}

/** Login is a form post; the backend replies 204 and sets the session cookie. */
export function login(email: string, password: string): Promise<void> {
  return apiFetch<void>('/auth/login', {
    method: 'POST',
    body: new URLSearchParams({ email, password }),
  });
}

export function logout(): Promise<void> {
  return apiFetch<void>('/auth/logout', { method: 'POST' });
}
