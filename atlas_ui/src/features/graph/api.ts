import { apiFetch } from '@/shared/api';

/** A directed, typed relationship between two movements. */
export type Edge = { source: string; target: string; type: string };

export type MovementEdges = { outgoing: Edge[]; incoming: Edge[] };

export type OutgoingEdge = { target: string; type: string };

export const getEdges = (movementKey: string) =>
  apiFetch<MovementEdges>(`/graph/movements/${movementKey}/edges`);

/** Replaces all edges leaving the movement. */
export const setOutgoing = (movementKey: string, edges: OutgoingEdge[]) =>
  apiFetch<void>(`/graph/movements/${movementKey}/outgoing`, {
    method: 'PUT',
    body: { edges },
  });
