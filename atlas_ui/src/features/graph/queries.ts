import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getEdgeTypes, getEdges, setOutgoing } from './api';
import type { OutgoingEdge } from './api';

const edgesKey = (movementKey: string) =>
  ['graph', 'edges', movementKey] as const;

export const useEdges = (movementKey: string) =>
  useQuery({
    queryKey: edgesKey(movementKey),
    queryFn: () => getEdges(movementKey),
  });

/** The types to offer. They rarely change, so they are fetched once. */
export const useEdgeTypes = () =>
  useQuery({
    queryKey: ['graph', 'edge-types'],
    queryFn: getEdgeTypes,
    staleTime: Infinity,
  });

export function useSetOutgoing() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (v: { movementKey: string; edges: OutgoingEdge[] }) =>
      setOutgoing(v.movementKey, v.edges),
    // Edges touch other movements' incoming lists too, so refresh every graph query.
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['graph'] }),
  });
}
