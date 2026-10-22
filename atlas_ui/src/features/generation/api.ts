import { apiFetch } from '@/shared/api';

export type Viewpoint = {
  id: string;
  name: string;
  specification: string;
  isDefault: boolean;
};
export type ViewpointInput = { name: string; specification: string };

export type GenerationStatus =
  | 'PENDING'
  | 'RUNNING'
  | 'READY_FOR_REVIEW'
  | 'APPLIED'
  | 'DISCARDED'
  | 'FAILED';

export type IllustrationStageInput = {
  avatarId: string;
  viewpointId: string;
  instructions: string;
};

export type IllustrationCandidate = {
  state: 'PENDING' | 'DONE' | 'FAILED';
  imageUrl: string | null;
  failureReason: string | null;
  progress: string | null;
};

export type GenerationMovementIllustrations = {
  id: string;
  movementKey: string;
  status: GenerationStatus;
  failureReason: string | null;
  createdAt: string;
  finishedAt: string | null;
  stages: (IllustrationStageInput & {
    position: number;
    candidate: IllustrationCandidate;
  })[];
};

export const listViewpoints = () => apiFetch<Viewpoint[]>('/viewpoints');

export const createViewpoint = (input: ViewpointInput) =>
  apiFetch<Viewpoint>('/viewpoints', { method: 'POST', body: input });

export const updateViewpoint = (id: string, input: ViewpointInput) =>
  apiFetch<Viewpoint>(`/viewpoints/${id}`, { method: 'PUT', body: input });

export const makeDefaultViewpoint = (id: string) =>
  apiFetch<Viewpoint>(`/viewpoints/${id}/default`, { method: 'PUT' });

export const deleteViewpoint = (id: string) =>
  apiFetch<void>(`/viewpoints/${id}`, { method: 'DELETE' });

const movementPath = (movementKey: string) => `/movements/${movementKey}`;

export const listGenerations = (movementKey: string) =>
  apiFetch<GenerationMovementIllustrations[]>(
    `${movementPath(movementKey)}/generations`,
  );

export const requestGeneration = (
  movementKey: string,
  stages: IllustrationStageInput[],
) =>
  apiFetch<GenerationMovementIllustrations>(
    `${movementPath(movementKey)}/generations`,
    { method: 'POST', body: { stages } },
  );

export const applyGeneration = (movementKey: string, id: string) =>
  apiFetch<GenerationMovementIllustrations>(
    `${movementPath(movementKey)}/generations/${id}/apply`,
    { method: 'POST' },
  );

export const discardGeneration = (movementKey: string, id: string) =>
  apiFetch<void>(`${movementPath(movementKey)}/generations/${id}/discard`, {
    method: 'POST',
  });

export const clearIllustrations = (movementKey: string) =>
  apiFetch<unknown>(`${movementPath(movementKey)}/illustrations`, {
    method: 'DELETE',
  });
