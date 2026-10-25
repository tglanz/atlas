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
import { Link, NavLink, Outlet } from 'react-router';
import { AccountMenu } from '@/features/auth';
import { navLinks } from './navLinks';
import classes from './AppLayout.module.css';

export function AppLayout() {
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
                  {navLinks.map((link) => (
                    <Menu.Item key={link.to} component={NavLink} to={link.to}>
                      {link.label}
                    </Menu.Item>
                  ))}
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
                {navLinks.map((link) => (
                  <li key={link.to}>
                    <NavLink to={link.to} className={classes.link}>
                      {link.label}
                    </NavLink>
                  </li>
                ))}
              </Group>
            </Box>
            <AccountMenu />
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
