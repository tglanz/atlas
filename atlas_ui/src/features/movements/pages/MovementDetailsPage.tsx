import { Button, Group, Image, List, Stack, Text, Title } from '@mantine/core';
import { Link, useParams } from 'react-router';
import { useCurrentUser } from '@/features/auth';
import { RelatedMovements } from '@/features/graph';
import { QueryBoundary, TagLink } from '@/shared/ui';
import type { Movement } from '@/shared/movements';
import { groupItems } from '../itemTypes';
import { useMovement } from '@/shared/movements';

export function MovementDetailsPage() {
  const { key = '' } = useParams();
  const movement = useMovement(key);

  return (
    <QueryBoundary query={movement}>
      {(data) => <MovementView movement={data} />}
    </QueryBoundary>
  );
}

function MovementView({ movement }: { movement: Movement }) {
  const user = useCurrentUser();

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="flex-start">
        <Title order={1}>{movement.name}</Title>
        {user.data?.role === 'MAINTAINER' && (
          <Button
            component={Link}
            to={`/movements/${movement.key}/edit`}
            size="xs"
          >
            Edit
          </Button>
        )}
      </Group>

      {movement.illustrations.length > 0 && (
        <Group gap="md" wrap="wrap">
          {movement.illustrations.map((url, index) => (
            <Image
              key={`${url}-${index}`}
              src={url}
              alt={`${movement.name}, step ${index + 1}`}
              h={220}
              w="auto"
              fit="contain"
            />
          ))}
        </Group>
      )}

      {movement.description && (
        <Text style={{ whiteSpace: 'pre-wrap' }}>{movement.description}</Text>
      )}

      {groupItems(movement.items).map((group) => (
        <section key={group.type}>
          <Title order={2} size="h3" mb="xs">
            {group.title}
          </Title>
          <List type={group.ordered ? 'ordered' : 'unordered'}>
            {group.items.map((item, index) => (
              <List.Item key={index}>{item.content}</List.Item>
            ))}
          </List>
        </section>
      ))}

      {movement.tags.length > 0 && (
        <Group gap="xs">
          {movement.tags.map((tag) => (
            <TagLink key={tag} tag={tag} />
          ))}
        </Group>
      )}
      <RelatedMovements movementKey={movement.key} />
    </Stack>
  );
}
