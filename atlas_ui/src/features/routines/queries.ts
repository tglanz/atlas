import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createRoutine,
  deleteRoutine,
  getRoutine,
  listRoutines,
  updateRoutine,
} from './api';
import type { RoutineInput } from './api';

const routinesKey = ['routines'] as const;

export const useRoutines = () =>
  useQuery({ queryKey: routinesKey, queryFn: listRoutines });

export const useRoutine = (id: string) =>
  useQuery({ queryKey: [...routinesKey, id], queryFn: () => getRoutine(id) });

function useRoutineMutation<V>(fn: (v: V) => Promise<unknown>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: fn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: routinesKey }),
  });
}

export const useCreateRoutine = () =>
  useRoutineMutation((v: RoutineInput) => createRoutine(v));

export const useUpdateRoutine = () =>
  useRoutineMutation((v: { id: string; input: RoutineInput }) =>
    updateRoutine(v.id, v.input),
  );

export const useDeleteRoutine = () =>
  useRoutineMutation((id: string) => deleteRoutine(id));
