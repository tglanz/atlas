import { QueryBoundary, SpecificationManager } from '@/shared/ui';
import {
  useAvatars,
  useCreateAvatar,
  useDeleteAvatar,
  useUpdateAvatar,
} from '../queries';

/** Maintainer view: manage the Avatars used to generate Illustrations. */
export function AvatarSettings() {
  const avatars = useAvatars();
  const create = useCreateAvatar();
  const update = useUpdateAvatar();
  const remove = useDeleteAvatar();

  return (
    <QueryBoundary query={avatars}>
      {(items) => (
        <SpecificationManager
          noun="avatar"
          items={items}
          onSave={(id, input) =>
            id ? update.mutateAsync({ id, input }) : create.mutateAsync(input)
          }
          onDelete={(id) => remove.mutateAsync(id)}
        />
      )}
    </QueryBoundary>
  );
}
