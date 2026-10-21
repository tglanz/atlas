import { apiFetch, isApiError } from '@/shared/api';

export type MovementLink = { target: string; relationType: string };

export type MovementContent = {
  name: string;
  description: string;
  cues: string[];
  tags: string[];
  links: MovementLink[];
};

export type MovementSummary = {
  key: string;
  revision: number;
  name: string;
  tags: string[];
  representativeIllustration: string | null;
};

/** A revision as viewers see it: the Illustrations of one Avatar, in order. */
export type MovementDetails = MovementContent & {
  key: string;
  revision: number;
  status: 'DRAFT' | 'PUBLISHED';
  publishedAt: string | null;
  avatarId: string | null;
  illustrations: string[];
};

/** A draft as maintainers see it: the Illustrations of every Avatar. */
export type MovementDraft = MovementContent & {
  key: string;
  revision: number;
  status: 'DRAFT' | 'PUBLISHED';
  publishedAt: string | null;
  illustrations: Record<string, { url: string }[]>;
};

export type RevisionSummary = {
  revision: number;
  name: string;
  publishedAt: string;
};

export type MovementFilters = { text?: string; tag?: string };

export function listMovements(filters: MovementFilters, signal?: AbortSignal) {
  const params = new URLSearchParams();
  if (filters.text) params.set('text', filters.text);
  if (filters.tag) params.set('tag', filters.tag);
  const query = params.size > 0 ? `?${params}` : '';
  return apiFetch<MovementSummary[]>(`/movements${query}`, { signal });
}

export const getMovement = (key: string) =>
  apiFetch<MovementDetails>(`/movements/${key}`);

export const getRevision = (key: string, revision: number) =>
  apiFetch<MovementDetails>(`/movements/${key}/revisions/${revision}`);

export const listRevisions = (key: string) =>
  apiFetch<RevisionSummary[]>(`/movements/${key}/revisions`);

/** The open draft, or null when there is none. Maintainers only. */
export async function getDraft(key: string): Promise<MovementDraft | null> {
  try {
    return await apiFetch<MovementDraft>(`/movements/${key}/draft`);
  } catch (error) {
    if (isApiError(error, 404)) return null;
    throw error;
  }
}

export const createMovement = (key: string, content: MovementContent) =>
  apiFetch<MovementDraft>('/movements', {
    method: 'POST',
    body: { key, content },
  });

export const updateDraft = (key: string, content: MovementContent) =>
  apiFetch<MovementDraft>(`/movements/${key}/draft`, {
    method: 'PUT',
    body: content,
  });

export const createRevision = (key: string) =>
  apiFetch<MovementDraft>(`/movements/${key}/revisions`, { method: 'POST' });

export const publishDraft = (key: string) =>
  apiFetch<MovementDraft>(`/movements/${key}/draft/publish`, {
    method: 'POST',
  });
