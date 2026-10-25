import { Button, Group, Menu } from '@mantine/core';
import { IconUser } from '@tabler/icons-react';
import { Link, useNavigate } from 'react-router';
import { useCurrentUser, useLogout } from '../queries';

export function AccountMenu() {
  const user = useCurrentUser();
  const logout = useLogout();
  const navigate = useNavigate();

  if (user.isPending) return null;
  if (!user.data) {
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

  return (
    <Menu position="bottom-end">
      <Menu.Target>
        <Button
          variant="default"
          size="xs"
          maw={160}
          leftSection={<IconUser size={16} />}
        >
          {user.data.email}
        </Button>
      </Menu.Target>
      <Menu.Dropdown>
        {user.data.role === 'MAINTAINER' && (
          <Menu.Item component={Link} to="/settings/illustrations">
            Illustration settings
          </Menu.Item>
        )}
        <Menu.Item
          onClick={() =>
            logout.mutate(undefined, { onSuccess: () => navigate('/') })
          }
        >
          Sign out
        </Menu.Item>
      </Menu.Dropdown>
    </Menu>
  );
}
