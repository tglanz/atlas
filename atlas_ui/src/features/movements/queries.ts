import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createMovement,
  getMovement,
  listMovements,
  updateMovement,
} from './api';
import type { MovementContent, MovementFilters } from './api';

export const movementKeys = {
  all: ['movements'] as const,
  list: (filters: MovementFilters) => ['movements', 'list', filters] as const,
  detail: (key: string) => ['movements', 'detail', key] as const,
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

/** A change to a movement can affect lists and details, so refresh them all. */
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

export const useUpdateMovement = () =>
  useMovementMutation((v: { key: string; content: MovementContent }) =>
    updateMovement(v.key, v.content),
  );
