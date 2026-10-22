import { Anchor, Card, Group, Image, Loader } from '@mantine/core';
import { Link } from 'react-router';
import type { MovementSummary } from '../movements';
import { useMovement } from '../movements';
import { TagLink } from './TagLink';

/** A movement at a glance: its first picture, its name linking to it, and its tags. */
export function MovementCard({ movement }: { movement: MovementSummary }) {
  const address = `/movements/${movement.key}`;
  return (
    <Card withBorder>
      {movement.representativeIllustration && (
        // The name below is the accessible link; the picture is a larger click target.
        <Card.Section component={Link} to={address} tabIndex={-1} aria-hidden>
          <Image
            src={movement.representativeIllustration}
            alt=""
            h={160}
            fit="contain"
          />
        </Card.Section>
      )}
      <Anchor component={Link} to={address} fw={700} mt="sm" display="block">
        {movement.name}
      </Anchor>
      <Group gap="xs" mt="xs">
        {movement.tags.map((tag) => (
          <TagLink key={tag} tag={tag} />
        ))}
      </Group>
    </Card>
  );
}

/** The same card for a movement known only by its key, such as one found through the graph. */
export function MovementCardByKey({ movementKey }: { movementKey: string }) {
  const movement = useMovement(movementKey);

  if (movement.isPending) {
    return (
      <Card withBorder>
        <Loader size="sm" aria-label="Loading" />
      </Card>
    );
  }
  if (movement.isError) {
    // Still offer the way to it, even though its details could not be loaded.
    return (
      <Card withBorder>
        <Anchor component={Link} to={`/movements/${movementKey}`} fw={700}>
          {movementKey}
        </Anchor>
      </Card>
    );
  }
  const { key, name, tags, illustrations } = movement.data;
  return (
    <MovementCard
      movement={{
        key,
        name,
        tags,
        representativeIllustration: illustrations[0] ?? null,
      }}
    />
  );
}
