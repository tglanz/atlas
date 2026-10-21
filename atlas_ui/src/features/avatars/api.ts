import { apiFetch } from '@/shared/api';

/** The specification is only returned to maintainers. */
export type Avatar = {
  id: string;
  name: string;
  specification?: string;
  isDefault: boolean;
};

export type AvatarInput = { name: string; specification: string };

export const listAvatars = () => apiFetch<Avatar[]>('/avatars');

export const createAvatar = (input: AvatarInput) =>
  apiFetch<Avatar>('/avatars', { method: 'POST', body: input });

export const updateAvatar = (id: string, input: AvatarInput) =>
  apiFetch<Avatar>(`/avatars/${id}`, { method: 'PUT', body: input });

export const deleteAvatar = (id: string) =>
  apiFetch<void>(`/avatars/${id}`, { method: 'DELETE' });

export const makeDefaultAvatar = (id: string) =>
  apiFetch<Avatar>(`/avatars/${id}/default`, { method: 'PUT' });
