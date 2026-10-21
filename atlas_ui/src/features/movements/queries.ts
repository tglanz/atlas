import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createMovement,
  createRevision,
  getDraft,
  getMovement,
  getRevision,
  listMovements,
  listRevisions,
  publishDraft,
  updateDraft,
} from './api';
import type { MovementContent, MovementFilters } from './api';

export const movementKeys = {
  all: ['movements'] as const,
  list: (filters: MovementFilters) => ['movements', 'list', filters] as const,
  detail: (key: string) => ['movements', 'detail', key] as const,
  revisions: (key: string) => ['movements', 'revisions', key] as const,
  revision: (key: string, revision: number) =>
    ['movements', 'revision', key, revision] as const,
  draft: (key: string) => ['movements', 'draft', key] as const,
};

export function useMovements(filters: MovementFilters) {
  return useQuery({
    queryKey: movementKeys.list(filters),
    queryFn: ({ signal }) => listMovements(filters, signal),
  });
}

export const useMovement = (key: string) =>
  useQuery({
    queryKey: movementKeys.detail(key),
    queryFn: () => getMovement(key),
  });

export const useRevision = (key: string, revision: number) =>
  useQuery({
    queryKey: movementKeys.revision(key, revision),
    queryFn: () => getRevision(key, revision),
  });

export const useRevisions = (key: string) =>
  useQuery({
    queryKey: movementKeys.revisions(key),
    queryFn: () => listRevisions(key),
  });

export const useDraft = (key: string, enabled = true) =>
  useQuery({
    queryKey: movementKeys.draft(key),
    queryFn: () => getDraft(key),
    enabled,
  });

/** Any change to a Movement can affect lists, details and history, so refresh them all. */
function useMovementMutation<V>(fn: (v: V) => Promise<unknown>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: fn,
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: movementKeys.all }),
  });
}

export const useCreateMovement = () =>
  useMovementMutation((v: { key: string; content: MovementContent }) =>
    createMovement(v.key, v.content),
  );

export const useUpdateDraft = () =>
  useMovementMutation((v: { key: string; content: MovementContent }) =>
    updateDraft(v.key, v.content),
  );

export const useCreateRevision = () =>
  useMovementMutation((key: string) => createRevision(key));

export const usePublishDraft = () =>
  useMovementMutation((key: string) => publishDraft(key));
