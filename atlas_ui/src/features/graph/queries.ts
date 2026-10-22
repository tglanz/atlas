import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getEdges, setOutgoing } from './api';
import type { OutgoingEdge } from './api';

const edgesKey = (movementKey: string) =>
  ['graph', 'edges', movementKey] as const;

export const useEdges = (movementKey: string) =>
  useQuery({
    queryKey: edgesKey(movementKey),
    queryFn: () => getEdges(movementKey),
  });

export function useSetOutgoing(movementKey: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (edges: OutgoingEdge[]) => setOutgoing(movementKey, edges),
    // Edges touch other movements' incoming lists too, so refresh every graph query.
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['graph'] }),
  });
}
