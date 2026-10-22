import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  clearIllustrations,
  createViewpoint,
  deleteViewpoint,
  listGenerations,
  listViewpoints,
  makeDefaultViewpoint,
  requestGeneration,
  retryGeneration,
  updateViewpoint,
  uploadMedia,
} from './api';
import type { StageSpec, ViewpointInput } from './api';

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

/** What a stage looks like in the form, before any picture is uploaded. */
export type StageInput = {
  instructions: string;
  /** A reference image (avatar-based stages) or the image itself (static stages). */
  file: File | null;
};

export type GenerationForm =
  | {
      mode: 'avatar';
      avatarId: string;
      viewpointId: string;
      stages: StageInput[];
    }
  | { mode: 'static'; stages: StageInput[] };

/** Uploads the pictures first, then requests the generation with their urls. */
export function useRequestGeneration(movementKey: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (form: GenerationForm) => {
      const stages: StageSpec[] = await Promise.all(
        form.stages.map(async (stage): Promise<StageSpec> => {
          const url = stage.file ? await uploadMedia(stage.file) : undefined;
          if (form.mode === 'static') {
            return { staticImage: { imageUrl: url ?? '' } };
          }
          return {
            avatar: {
              avatarId: form.avatarId,
              viewpointId: form.viewpointId,
              instructions: stage.instructions.trim() || undefined,
              referenceImageUrl: url,
            },
          };
        }),
      );
      return requestGeneration(movementKey, stages);
    },
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

export const useClearIllustrations = (movementKey: string) =>
  useMutation({ mutationFn: () => clearIllustrations(movementKey) });
