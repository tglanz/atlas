import { Anchor, Text } from '@mantine/core';
import { Link } from 'react-router';
import { useMovement } from '@/shared/movements';

/** Shows a movement's current name, optionally linking to it. */
export function MovementRefName({
  movementKey,
  link = true,
}: {
  movementKey: string;
  link?: boolean;
}) {
  const movement = useMovement(movementKey);
  const name = movement.data?.name ?? movementKey;
  if (!link) return <Text span>{name}</Text>;
  return (
    <Anchor component={Link} to={`/movements/${movementKey}`}>
      {name}
    </Anchor>
  );
}
