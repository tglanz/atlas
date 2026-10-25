import { Button, Group } from '@mantine/core';
import { Link } from 'react-router';
import { useCurrentUser } from '../queries';

/** Sign in and register, for a visitor. A signed-in user signs out from the main navigation. */
export function AuthButtons() {
  const user = useCurrentUser();

  if (user.isPending || user.data) return null;
  return (
    <Group gap="xs" wrap="nowrap">
      <Button component={Link} to="/login" variant="default" size="xs">
        Sign in
      </Button>
      <Button component={Link} to="/register" size="xs">
        Register
      </Button>
    </Group>
  );
}
