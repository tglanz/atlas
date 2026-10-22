import { apiFetch } from '@/shared/api';

export type MovementLink = { target: string; relationType: string };

export type MovementContent = {
  name: string;
  description: string;
  cues: string[];
  tags: string[];
  links: MovementLink[];
};

/** A movement, with the urls of its Illustrations in order. */
export type Movement = MovementContent & {
  key: string;
  illustrations: string[];
};

export type MovementSummary = {
  key: string;
  name: string;
  tags: string[];
  representativeIllustration: string | null;
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
  apiFetch<Movement>(`/movements/${key}`);

export const createMovement = (key: string, content: MovementContent) =>
  apiFetch<Movement>('/movements', { method: 'POST', body: { key, content } });

export const updateMovement = (key: string, content: MovementContent) =>
  apiFetch<Movement>(`/movements/${key}`, { method: 'PUT', body: content });
