import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  fetchCurrentUser,
  login,
  logout,
  register,
  setPreferredAvatar,
} from './api';

export const currentUserKey = ['auth', 'me'] as const;

/** The signed-in user (null for visitors). The session cookie is the source of truth. */
export function useCurrentUser() {
  return useQuery({
    queryKey: currentUserKey,
    queryFn: fetchCurrentUser,
    staleTime: Infinity,
    retry: false,
  });
}

export function useLogin() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (v: { email: string; password: string }) =>
      login(v.email, v.password),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: currentUserKey }),
  });
}

export function useRegister() {
  return useMutation({
    mutationFn: (v: { email: string; password: string }) =>
      register(v.email, v.password),
  });
}

export function useLogout() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: logout,
    // Everything cached may depend on who is signed in.
    onSuccess: () => queryClient.clear(),
  });
}

export function useSetPreferredAvatar() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: setPreferredAvatar,
    // The server picks the Illustrations to show, so cached Movements are stale too.
    onSuccess: () => queryClient.invalidateQueries(),
  });
}
