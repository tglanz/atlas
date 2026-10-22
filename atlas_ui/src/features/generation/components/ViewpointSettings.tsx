import { Badge, Button } from '@mantine/core';
import { QueryBoundary, SpecificationManager } from '@/shared/ui';
import {
  useCreateViewpoint,
  useDeleteViewpoint,
  useMakeDefaultViewpoint,
  useUpdateViewpoint,
  useViewpoints,
} from '../queries';

/** Viewpoints, one of which is the default that generation preselects. */
export function ViewpointSettings() {
  const viewpoints = useViewpoints();
  const create = useCreateViewpoint();
  const update = useUpdateViewpoint();
  const remove = useDeleteViewpoint();
  const makeDefault = useMakeDefaultViewpoint();

  return (
    <QueryBoundary query={viewpoints}>
      {(items) => (
        <SpecificationManager
          noun="viewpoint"
          items={items}
          onSave={(id, input) =>
            id ? update.mutateAsync({ id, input }) : create.mutateAsync(input)
          }
          onDelete={(id) => remove.mutateAsync(id)}
          extra={(viewpoint) =>
            viewpoint.isDefault ? (
              <Badge>Default</Badge>
            ) : (
              <Button
                size="compact-xs"
                variant="subtle"
                loading={makeDefault.isPending}
                onClick={() => makeDefault.mutate(viewpoint.id)}
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
