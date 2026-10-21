import { QueryBoundary, SpecificationManager } from '@/shared/ui';
import {
  useCreateViewpoint,
  useDeleteViewpoint,
  useUpdateViewpoint,
  useViewpoints,
} from '../queries';

export function ViewpointSettings() {
  const viewpoints = useViewpoints();
  const create = useCreateViewpoint();
  const update = useUpdateViewpoint();
  const remove = useDeleteViewpoint();

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
        />
      )}
    </QueryBoundary>
  );
}
