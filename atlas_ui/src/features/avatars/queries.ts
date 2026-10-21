import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createAvatar,
  deleteAvatar,
  listAvatars,
  makeDefaultAvatar,
  updateAvatar,
} from './api';
import type { AvatarInput } from './api';

const avatarsKey = ['avatars'] as const;

export const useAvatars = () =>
  useQuery({ queryKey: avatarsKey, queryFn: listAvatars });

function useAvatarMutation<V>(fn: (v: V) => Promise<unknown>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: fn,
    // Which Avatar is default decides what Movements show, so refresh everything.
    onSuccess: () => queryClient.invalidateQueries(),
  });
}

export const useCreateAvatar = () =>
  useAvatarMutation((v: AvatarInput) => createAvatar(v));

export const useUpdateAvatar = () =>
  useAvatarMutation((v: { id: string; input: AvatarInput }) =>
    updateAvatar(v.id, v.input),
  );

export const useDeleteAvatar = () =>
  useAvatarMutation((id: string) => deleteAvatar(id));

export const useMakeDefaultAvatar = () =>
  useAvatarMutation((id: string) => makeDefaultAvatar(id));
