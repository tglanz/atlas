import { apiFetch } from '@/shared/api';

/** A piece of text about a movement. New types can be added without changing the model. */
export type MovementItem = { type: string; content: string };

/** A position of a movement worth showing, such as the bottom of a push-up. */
export type MovementStage = { name: string; description: string };

export type MovementContent = {
  name: string;
  description: string;
  items: MovementItem[];
  stages: MovementStage[];
  tags: string[];
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

/** A Movement must contain the text in its name and have all of the tags. */
export type MovementFilters = { text?: string; tags?: string[] };

export type MovementName = { key: string; name: string };

export type TagCount = { tag: string; count: number };

export function listMovements(filters: MovementFilters, signal?: AbortSignal) {
  const params = new URLSearchParams();
  if (filters.text) params.set('text', filters.text);
  filters.tags?.forEach((tag) => params.append('tags', tag));
  const query = params.size > 0 ? `?${params}` : '';
  return apiFetch<MovementSummary[]>(`/movements${query}`, { signal });
}

export const getMovement = (key: string) =>
  apiFetch<Movement>(`/movements/${key}`);

export const createMovement = (key: string, content: MovementContent) =>
  apiFetch<Movement>('/movements', { method: 'POST', body: { key, content } });

export const updateMovement = (key: string, content: MovementContent) =>
  apiFetch<Movement>(`/movements/${key}`, { method: 'PUT', body: content });

/** Every movement's name, for suggesting while typing. */
export const getMovementNames = () =>
  apiFetch<MovementName[]>('/movement-index/names');

/** Every tag in use, with how many movements have it. */
export const getMovementTags = () =>
  apiFetch<TagCount[]>('/movement-index/tags');
