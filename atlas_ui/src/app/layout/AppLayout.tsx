import {
  Anchor,
  AppShell,
  Box,
  Burger,
  Container,
  Group,
  Menu,
  Text,
} from '@mantine/core';
import { Link, NavLink, Outlet, useNavigate } from 'react-router';
import { AuthButtons, useCurrentUser, useLogout } from '@/features/auth';
import { maintainerLinks, navLinks } from './navLinks';
import classes from './AppLayout.module.css';

export function AppLayout() {
  const user = useCurrentUser();
  const logout = useLogout();
  const navigate = useNavigate();
  const links = [
    ...navLinks,
    ...(user.data?.role === 'MAINTAINER' ? maintainerLinks : []),
  ];
  const signedIn = Boolean(user.data);
  const signOut = () =>
    logout.mutate(undefined, { onSuccess: () => navigate('/') });

  return (
    <AppShell header={{ height: 60 }} padding="md">
      <a href="#main" className={classes.skip}>
        Skip to content
      </a>
      <AppShell.Header>
        <Container size="lg" h="100%">
          <Group h="100%" justify="space-between" wrap="nowrap">
            <Group gap="xs" wrap="nowrap">
              {/* On a narrow screen the links do not fit in the header, so they move into a menu. */}
              <Menu position="bottom-start">
                <Menu.Target>
                  <Burger hiddenFrom="sm" size="sm" aria-label="Navigation" />
                </Menu.Target>
                <Menu.Dropdown>
                  {links.map((link) => (
                    <Menu.Item key={link.to} component={NavLink} to={link.to}>
                      {link.label}
                    </Menu.Item>
                  ))}
                  {signedIn && (
                    <Menu.Item onClick={signOut}>Sign out</Menu.Item>
                  )}
                </Menu.Dropdown>
              </Menu>
              <Anchor
                component={Link}
                to="/"
                underline="never"
                aria-label="Form Atlas home"
              >
                <Text fw={800} size="lg" className={classes.brand}>
                  Form Atlas
                </Text>
              </Anchor>
            </Group>
            <Box component="nav" aria-label="Main" visibleFrom="sm">
              <Group
                gap="xs"
                wrap="nowrap"
                component="ul"
                className={classes.list}
              >
                {links.map((link) => (
                  <li key={link.to}>
                    <NavLink to={link.to} className={classes.link}>
                      {link.label}
                    </NavLink>
                  </li>
                ))}
                {signedIn && (
                  <li>
                    <button
                      type="button"
                      className={`${classes.link} ${classes.action}`}
                      onClick={signOut}
                    >
                      Sign out
                    </button>
                  </li>
                )}
              </Group>
            </Box>
            <AuthButtons />
          </Group>
        </Container>
      </AppShell.Header>
      <AppShell.Main id="main">
        <Container size="lg">
          <Outlet />
        </Container>
      </AppShell.Main>
    </AppShell>
  );
}
