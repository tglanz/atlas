import { Stack, Text, Title } from '@mantine/core';

export function ComingSoonPage({ title }: { title: string }) {
  return (
    <Stack py="xl" gap="xs">
      <Title order={1}>{title}</Title>
      <Text c="dimmed">Coming soon.</Text>
    </Stack>
  );
}
