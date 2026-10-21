import { Button, Stack, Text, Title } from '@mantine/core';
import { Link } from 'react-router';

export function NotFoundPage() {
  return (
    <Stack py="xl" gap="xs" align="flex-start">
      <Title order={1}>Page not found</Title>
      <Text c="dimmed">The page you are looking for does not exist.</Text>
      <Button component={Link} to="/" variant="light">
        Back to home
      </Button>
    </Stack>
  );
}
