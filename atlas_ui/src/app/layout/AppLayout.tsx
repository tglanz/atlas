import { Anchor, AppShell, Container, Group, Text } from '@mantine/core';
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
            <Anchor
              component={Link}
              to="/"
              underline="never"
              aria-label="Form Atlas home"
            >
              <Text fw={800} size="lg">
                Form Atlas
              </Text>
            </Anchor>
            <nav aria-label="Main">
              <Group gap="xs" component="ul" className={classes.list}>
                {navLinks.map((link) => (
                  <li key={link.to}>
                    <NavLink to={link.to} className={classes.link}>
                      {link.label}
                    </NavLink>
                  </li>
                ))}
              </Group>
            </nav>
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
