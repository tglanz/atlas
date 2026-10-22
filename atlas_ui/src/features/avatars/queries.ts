import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createAvatar, deleteAvatar, listAvatars, updateAvatar } from './api';
import type { AvatarInput } from './api';

const avatarsKey = ['avatars'] as const;

export const useAvatars = () =>
  useQuery({ queryKey: avatarsKey, queryFn: listAvatars });

function useAvatarMutation<V>(fn: (v: V) => Promise<unknown>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: fn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: avatarsKey }),
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
