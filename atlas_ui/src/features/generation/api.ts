import { apiFetch } from '@/shared/api';

export type Viewpoint = {
  id: string;
  name: string;
  specification: string;
  isDefault: boolean;
};
export type ViewpointInput = { name: string; specification: string };

/** How one Illustration is produced. Exactly one of the two fields is set. */
export type StageSpec =
  | {
      avatar: {
        avatarId: string;
        viewpointId: string;
        instructions?: string;
        referenceImageUrl?: string;
      };
    }
  | { staticImage: { imageUrl: string } };

export type GenerationStatus = 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED';

export type GenerationRequest = {
  id: string;
  stages: Partial<{ avatar: unknown; staticImage: unknown }>[];
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

export const makeDefaultViewpoint = (id: string) =>
  apiFetch<Viewpoint>(`/viewpoints/${id}/default`, { method: 'PUT' });

export const deleteViewpoint = (id: string) =>
  apiFetch<void>(`/viewpoints/${id}`, { method: 'DELETE' });

/** Stores a picture and returns its url, to use as an image in a spec. */
export async function uploadMedia(file: File): Promise<string> {
  const body = new FormData();
  body.append('file', file);
  const { url } = await apiFetch<{ url: string }>('/media', {
    method: 'POST',
    body,
  });
  return url;
}

const movementPath = (movementKey: string) => `/movements/${movementKey}`;

export const listGenerations = (movementKey: string) =>
  apiFetch<GenerationRequest[]>(`${movementPath(movementKey)}/generations`);

/** Produces the movement's whole Illustration sequence: one picture per stage. */
export const requestGeneration = (movementKey: string, stages: StageSpec[]) =>
  apiFetch<GenerationRequest>(`${movementPath(movementKey)}/generations`, {
    method: 'POST',
    body: { stages },
  });

export const retryGeneration = (movementKey: string, id: string) =>
  apiFetch<GenerationRequest>(
    `${movementPath(movementKey)}/generations/${id}/retry`,
    {
      method: 'POST',
    },
  );

export const clearIllustrations = (movementKey: string) =>
  apiFetch<unknown>(`${movementPath(movementKey)}/illustrations`, {
    method: 'DELETE',
  });
