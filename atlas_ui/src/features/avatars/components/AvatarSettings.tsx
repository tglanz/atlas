import { Badge, Button } from '@mantine/core';
import { QueryBoundary, SpecificationManager } from '@/shared/ui';
import {
  useAvatars,
  useCreateAvatar,
  useDeleteAvatar,
  useMakeDefaultAvatar,
  useUpdateAvatar,
} from '../queries';

/** Maintainer view: manage Avatars and choose the default one. */
export function AvatarSettings() {
  const avatars = useAvatars();
  const create = useCreateAvatar();
  const update = useUpdateAvatar();
  const remove = useDeleteAvatar();
  const makeDefault = useMakeDefaultAvatar();

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
          extra={(avatar) =>
            avatar.isDefault ? (
              <Badge>Default</Badge>
            ) : (
              <Button
                size="compact-xs"
                variant="subtle"
                loading={makeDefault.isPending}
                onClick={() => makeDefault.mutate(avatar.id)}
              >
                Make default
              </Button>
            )
          }
        />
      )}
    </QueryBoundary>
  );
}
