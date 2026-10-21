import { Anchor, Text } from '@mantine/core';
import { Link } from 'react-router';
import { useRevision } from '@/features/movements';
import type { MovementRef } from '../api';

/** Shows the name of the pinned revision, linking to that exact revision. */
export function MovementRefName({
  movement,
  link = true,
}: {
  movement: MovementRef;
  link?: boolean;
}) {
  const revision = useRevision(movement.key, movement.revision);
  const name = revision.data?.name ?? movement.key;
  const label = `${name} (revision ${movement.revision})`;
  if (!link) return <Text span>{label}</Text>;
  return (
    <Anchor
      component={Link}
      to={`/movements/${movement.key}/revisions/${movement.revision}`}
    >
      {label}
    </Anchor>
  );
}
