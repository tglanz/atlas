import { apiFetch } from '@/shared/api';

/** A section lists movements by key, in order. */
export type Section = {
  name: string;
  description: string;
  movements: string[];
};

export type RoutineInput = {
  name: string;
  description: string;
  sections: Section[];
};

export type Routine = RoutineInput & { id: string };

export type RoutineSummary = { id: string; name: string; description: string };

export const listRoutines = () => apiFetch<RoutineSummary[]>('/routines');

export const getRoutine = (id: string) => apiFetch<Routine>(`/routines/${id}`);

export const createRoutine = (input: RoutineInput) =>
  apiFetch<Routine>('/routines', { method: 'POST', body: input });

export const updateRoutine = (id: string, input: RoutineInput) =>
  apiFetch<Routine>(`/routines/${id}`, { method: 'PUT', body: input });

export const deleteRoutine = (id: string) =>
  apiFetch<void>(`/routines/${id}`, { method: 'DELETE' });
