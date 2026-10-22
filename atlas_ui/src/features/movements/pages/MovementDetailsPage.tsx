import {
  Anchor,
  Badge,
  Button,
  Group,
  Image,
  List,
  Stack,
  Text,
  Title,
} from '@mantine/core';
import { Link, useParams } from 'react-router';
import { useCurrentUser } from '@/features/auth';
import { QueryBoundary } from '@/shared/ui';
import type { Movement, MovementLink } from '../api';
import { useMovement } from '../queries';

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

  // Links are grouped by relationship, such as progression or variant.
  const linksByType = [
    ...movement.links
      .reduce((groups, link) => {
        groups.set(link.relationType, [
          ...(groups.get(link.relationType) ?? []),
          link,
        ]);
        return groups;
      }, new Map<string, MovementLink[]>())
      .entries(),
  ];

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

      {movement.cues.length > 0 && (
        <section>
          <Title order={2} size="h3" mb="xs">
            Cues
          </Title>
          <List type="ordered">
            {movement.cues.map((cue, index) => (
              <List.Item key={index}>{cue}</List.Item>
            ))}
          </List>
        </section>
      )}

      {movement.tags.length > 0 && (
        <Group gap="xs">
          {movement.tags.map((tag) => (
            <Badge
              key={tag}
              variant="light"
              component={Link}
              to={`/movements?tag=${encodeURIComponent(tag)}`}
            >
              {tag}
            </Badge>
          ))}
        </Group>
      )}

      {linksByType.map(([relationType, links]) => (
        <section key={relationType}>
          <Title
            order={2}
            size="h3"
            mb="xs"
            style={{ textTransform: 'capitalize' }}
          >
            {relationType}
          </Title>
          <Group gap="md">
            {links.map((link) => (
              <Anchor
                key={link.target}
                component={Link}
                to={`/movements/${link.target}`}
              >
                {link.target}
              </Anchor>
            ))}
          </Group>
        </section>
      ))}
    </Stack>
  );
}
