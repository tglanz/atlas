import { Anchor, Stack, Text, Title } from '@mantine/core';
import { Link, useParams } from 'react-router';
import { QueryBoundary } from '@/shared/ui';
import { useRevisions } from '../queries';

export function RevisionHistoryPage() {
  const { key = '' } = useParams();
  const revisions = useRevisions(key);

  return (
    <Stack gap="md">
      <Title order={1}>Revision history</Title>
      <Anchor component={Link} to={`/movements/${key}`}>
        Back to {key}
      </Anchor>
      <QueryBoundary query={revisions}>
        {(items) => (
          <Stack
            gap="xs"
            component="ul"
            style={{ listStyle: 'none', padding: 0 }}
          >
            {[...items].reverse().map((item) => (
              <li key={item.revision}>
                <Anchor
                  component={Link}
                  to={`/movements/${key}/revisions/${item.revision}`}
                >
                  Revision {item.revision}: {item.name}
                </Anchor>
                <Text size="sm" c="dimmed">
                  Published {new Date(item.publishedAt).toLocaleString()}
                </Text>
              </li>
            ))}
          </Stack>
        )}
      </QueryBoundary>
    </Stack>
  );
}
