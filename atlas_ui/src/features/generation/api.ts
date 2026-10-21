import { apiFetch } from '@/shared/api';

export type Viewpoint = { id: string; name: string; specification: string };
export type ViewpointInput = { name: string; specification: string };

export type GenerationStatus = 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED';

/** A stage is described in words, by a reference picture url, or both. */
export type Stage = { description: string; referenceUrl: string };

export type GenerationRequest = {
  id: string;
  avatarId: string;
  viewpointId: string;
  stages: Stage[];
  status: GenerationStatus;
  failureReason: string | null;
  results: string[];
  createdAt: string;
};

export const listViewpoints = () => apiFetch<Viewpoint[]>('/viewpoints');

export const createViewpoint = (input: ViewpointInput) =>
  apiFetch<Viewpoint>('/viewpoints', { method: 'POST', body: input });

export const updateViewpoint = (id: string, input: ViewpointInput) =>
  apiFetch<Viewpoint>(`/viewpoints/${id}`, { method: 'PUT', body: input });

export const deleteViewpoint = (id: string) =>
  apiFetch<void>(`/viewpoints/${id}`, { method: 'DELETE' });

const draftPath = (movementKey: string) => `/movements/${movementKey}/draft`;

export const listGenerations = (movementKey: string) =>
  apiFetch<GenerationRequest[]>(`${draftPath(movementKey)}/generations`);

export const requestGeneration = (
  movementKey: string,
  body: { avatarId: string; viewpointId: string; stages: Stage[] },
) =>
  apiFetch<GenerationRequest>(`${draftPath(movementKey)}/generations`, {
    method: 'POST',
    body,
  });

export const retryGeneration = (movementKey: string, id: string) =>
  apiFetch<GenerationRequest>(
    `${draftPath(movementKey)}/generations/${id}/retry`,
    {
      method: 'POST',
    },
  );

export function uploadIllustration(
  movementKey: string,
  avatarId: string,
  file: File,
) {
  const body = new FormData();
  body.append('file', file);
  return apiFetch<{ url: string }>(
    `${draftPath(movementKey)}/avatars/${avatarId}/illustrations`,
    {
      method: 'POST',
      body,
    },
  );
}

export const clearIllustrations = (movementKey: string, avatarId: string) =>
  apiFetch<unknown>(
    `${draftPath(movementKey)}/avatars/${avatarId}/illustrations`,
    {
      method: 'DELETE',
    },
  );
