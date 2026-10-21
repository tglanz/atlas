import {
  Alert,
  Anchor,
  Button,
  Card,
  Group,
  Modal,
  Stack,
  Text,
  Title,
} from '@mantine/core';
import { useState } from 'react';
import { Link } from 'react-router';
import { QueryBoundary } from '@/shared/ui';
import type { RoutineSummary } from '../api';
import { useDeleteRoutine, useRoutines } from '../queries';

export function RoutineLibraryPage() {
  const routines = useRoutines();
  const remove = useDeleteRoutine();
  const [deleting, setDeleting] = useState<RoutineSummary | null>(null);

  return (
    <Stack gap="lg">
      <Group justify="space-between">
        <Title order={1}>Routines</Title>
        <Button component={Link} to="/routines/new">
          New routine
        </Button>
      </Group>
      <QueryBoundary query={routines}>
        {(items) =>
          items.length === 0 ? (
            <Text c="dimmed">You have no routines yet.</Text>
          ) : (
            <Stack gap="sm">
              {items.map((routine) => (
                <Card key={routine.id} withBorder padding="sm">
                  <Group justify="space-between" wrap="nowrap">
                    <div>
                      <Anchor
                        component={Link}
                        to={`/routines/${routine.id}`}
                        fw={600}
                      >
                        {routine.name}
                      </Anchor>
                      {routine.description && (
                        <Text size="sm" c="dimmed" lineClamp={2}>
                          {routine.description}
                        </Text>
                      )}
                    </div>
                    <Button
                      size="xs"
                      variant="default"
                      onClick={() => setDeleting(routine)}
                    >
                      Delete
                    </Button>
                  </Group>
                </Card>
              ))}
            </Stack>
          )
        }
      </QueryBoundary>

      <Modal
        opened={deleting !== null}
        onClose={() => setDeleting(null)}
        title="Delete routine?"
      >
        <Stack>
          <Text>Delete {deleting?.name}? This cannot be undone.</Text>
          {remove.isError && <Alert color="red">{remove.error.message}</Alert>}
          <Group justify="flex-end">
            <Button variant="default" onClick={() => setDeleting(null)}>
              Cancel
            </Button>
            <Button
              color="red"
              loading={remove.isPending}
              onClick={() =>
                deleting &&
                remove.mutate(deleting.id, {
                  onSuccess: () => setDeleting(null),
                })
              }
            >
              Delete
            </Button>
          </Group>
        </Stack>
      </Modal>
    </Stack>
  );
}
