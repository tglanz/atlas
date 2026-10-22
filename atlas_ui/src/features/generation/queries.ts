import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  applyGeneration,
  clearIllustrations,
  createViewpoint,
  deleteViewpoint,
  listGenerations,
  listViewpoints,
  makeDefaultViewpoint,
  discardGeneration,
  requestGeneration,
  updateViewpoint,
} from './api';
import type { ViewpointInput } from './api';

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

export const useMakeDefaultViewpoint = () =>
  useViewpointMutation((id: string) => makeDefaultViewpoint(id));

export const useDeleteViewpoint = () =>
  useViewpointMutation((id: string) => deleteViewpoint(id));

/** Polls while a generation is in progress, so each image shows as soon as it is done. */
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

function useGenerationMutation<V>(
  movementKey: string,
  fn: (v: V) => Promise<unknown>,
) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: fn,
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: generationsKey(movementKey) }),
  });
}

export const useRequestGeneration = (movementKey: string) =>
  useGenerationMutation(
    movementKey,
    (input: { avatarId: string; viewpointId: string }) =>
      requestGeneration(movementKey, input),
  );

export const useApplyGeneration = (movementKey: string) =>
  useGenerationMutation(movementKey, (id: string) =>
    applyGeneration(movementKey, id),
  );

export const useDiscardGeneration = (movementKey: string) =>
  useGenerationMutation(movementKey, (id: string) =>
    discardGeneration(movementKey, id),
  );

export const useClearIllustrations = (movementKey: string) =>
  useMutation({ mutationFn: () => clearIllustrations(movementKey) });
