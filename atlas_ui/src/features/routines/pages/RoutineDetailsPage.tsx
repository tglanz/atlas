import { Button, Group, List, Stack, Text, Title } from '@mantine/core';
import { Link, useParams } from 'react-router';
import { QueryBoundary } from '@/shared/ui';
import { MovementRefName } from '../components/MovementRefName';
import { useRoutine } from '../queries';

export function RoutineDetailsPage() {
  const { id = '' } = useParams();
  const routine = useRoutine(id);

  return (
    <QueryBoundary query={routine}>
      {(data) => (
        <Stack gap="lg">
          <Group justify="space-between" align="flex-start">
            <div>
              <Title order={1}>{data.name}</Title>
              {data.description && <Text c="dimmed">{data.description}</Text>}
            </div>
            <Button component={Link} to={`/routines/${data.id}/edit`} size="xs">
              Edit
            </Button>
          </Group>
          {data.sections.length === 0 && (
            <Text c="dimmed">This routine has no sections.</Text>
          )}
          {data.sections.map((section, index) => (
            <section key={index}>
              <Title order={2} size="h3">
                {section.name}
              </Title>
              {section.description && (
                <Text c="dimmed">{section.description}</Text>
              )}
              {section.movements.length === 0 ? (
                <Text c="dimmed">No movements.</Text>
              ) : (
                <List type="ordered" mt="xs">
                  {section.movements.map((movement, i) => (
                    <List.Item key={i}>
                      <MovementRefName movement={movement} />
                    </List.Item>
                  ))}
                </List>
              )}
            </section>
          ))}
        </Stack>
      )}
    </QueryBoundary>
  );
}
