import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import {
  createMovement,
  getMovementNames,
  getMovementTags,
  getMovement,
  listMovements,
  updateMovement,
} from './api';
import type { MovementContent, MovementFilters } from './api';

export const movementKeys = {
  all: ['movements'] as const,
  list: (filters: MovementFilters) => ['movements', 'list', filters] as const,
  names: ['movements', 'index', 'names'] as const,
  tags: ['movements', 'index', 'tags'] as const,
  detail: (key: string) => ['movements', 'detail', key] as const,
};

export function useMovements(filters: MovementFilters) {
  return useQuery({
    queryKey: movementKeys.list(filters),
    queryFn: ({ signal }) => listMovements(filters, signal),
    // Keep showing the previous results while a new search loads, so the list does not flicker.
    placeholderData: keepPreviousData,
  });
}

/** Both lists change when a movement is created or edited, which refreshes them with the rest. */
export const useMovementNames = () =>
  useQuery({ queryKey: movementKeys.names, queryFn: getMovementNames });

export const useMovementTags = () =>
  useQuery({ queryKey: movementKeys.tags, queryFn: getMovementTags });

export const useMovement = (key: string, enabled = true) =>
  useQuery({
    queryKey: movementKeys.detail(key),
    queryFn: () => getMovement(key),
    enabled: enabled && key !== '',
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
