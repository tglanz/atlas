import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  clearIllustrations,
  createViewpoint,
  deleteViewpoint,
  listGenerations,
  listViewpoints,
  requestGeneration,
  retryGeneration,
  updateViewpoint,
  uploadIllustration,
} from './api';
import type { Stage, ViewpointInput } from './api';

const viewpointsKey = ['viewpoints'] as const;
const generationsKey = (movementKey: string) =>
  ['generations', movementKey] as const;

export const useViewpoints = () =>
  useQuery({ queryKey: viewpointsKey, queryFn: listViewpoints });

function useViewpointMutation<V>(fn: (v: V) => Promise<unknown>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: fn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: viewpointsKey }),
  });
}

export const useCreateViewpoint = () =>
  useViewpointMutation((v: ViewpointInput) => createViewpoint(v));

export const useUpdateViewpoint = () =>
  useViewpointMutation((v: { id: string; input: ViewpointInput }) =>
    updateViewpoint(v.id, v.input),
  );

export const useDeleteViewpoint = () =>
  useViewpointMutation((id: string) => deleteViewpoint(id));

/** Polls while any request is still running, so progress shows without a refresh. */
export const useGenerations = (movementKey: string) =>
  useQuery({
    queryKey: generationsKey(movementKey),
    queryFn: () => listGenerations(movementKey),
    refetchInterval: (query) =>
      query.state.data?.some(
        (r) => r.status === 'PENDING' || r.status === 'RUNNING',
      )
        ? 2000
        : false,
  });

export function useRequestGeneration(movementKey: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: {
      avatarId: string;
      viewpointId: string;
      stages: Stage[];
    }) => requestGeneration(movementKey, body),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: generationsKey(movementKey) }),
  });
}

export function useRetryGeneration(movementKey: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => retryGeneration(movementKey, id),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: generationsKey(movementKey) }),
  });
}

export const useUploadIllustration = (movementKey: string) =>
  useMutation({
    mutationFn: (v: { avatarId: string; file: File }) =>
      uploadIllustration(movementKey, v.avatarId, v.file),
  });

export const useClearIllustrations = (movementKey: string) =>
  useMutation({
    mutationFn: (avatarId: string) => clearIllustrations(movementKey, avatarId),
  });
