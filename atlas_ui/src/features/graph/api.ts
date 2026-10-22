import { apiFetch } from '@/shared/api';

/** A directed, typed relationship between two movements. */
export type Edge = { source: string; target: string; type: string };

export type MovementEdges = { outgoing: Edge[]; incoming: Edge[] };

export type OutgoingEdge = { target: string; type: string };

/** A relationship type to offer. Edges may carry other types; this list is not a restriction. */
export type EdgeType = {
  name: string;
  label: string;
  /** The section title when a movement has several, such as Progressions. */
  plural: string;
  /** The type the other movement gets when this one is added; null for a one-way type. */
  inverse: string | null;
};

export const getEdgeTypes = () => apiFetch<EdgeType[]>('/graph/edge-types');

export const getEdges = (movementKey: string) =>
  apiFetch<MovementEdges>(`/graph/movements/${movementKey}/edges`);

/** Replaces all edges leaving the movement. */
export const setOutgoing = (movementKey: string, edges: OutgoingEdge[]) =>
  apiFetch<void>(`/graph/movements/${movementKey}/outgoing`, {
    method: 'PUT',
    body: { edges },
  });
