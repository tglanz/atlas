import {
  Alert,
  Badge,
  Card,
  Group,
  Image,
  Loader,
  SimpleGrid,
  Stack,
  Text,
  TextInput,
  Title,
} from '@mantine/core';
import { IconSearch } from '@tabler/icons-react';
import { useSearchParams } from 'react-router';
import { useMovements } from '../queries';

export function MovementLibraryPage() {
  // Filters live in the URL so they survive a refresh and can be shared.
  const [params, setParams] = useSearchParams();
  const text = params.get('text') ?? '';
  const tag = params.get('tag') ?? '';
  const { data, isPending, isError, error } = useMovements({ text, tag });

  function setFilter(name: 'text' | 'tag', value: string) {
    const next = new URLSearchParams(params);
    if (value) next.set(name, value);
    else next.delete(name);
    setParams(next, { replace: true });
  }

  return (
    <Stack gap="lg">
      <Title order={1}>Movements</Title>
      <Group align="flex-end">
        <TextInput
          label="Search by name"
          leftSection={<IconSearch size={16} />}
          value={text}
          onChange={(e) => setFilter('text', e.currentTarget.value)}
        />
        {tag && (
          <Badge
            size="lg"
            variant="light"
            style={{ cursor: 'pointer' }}
            onClick={() => setFilter('tag', '')}
          >
            Tag: {tag} (clear)
          </Badge>
        )}
      </Group>

      {isPending && <Loader aria-label="Loading movements" />}
      {isError && (
        <Alert color="red" title="Could not load movements">
          {error.message}
        </Alert>
      )}
      {data?.length === 0 && <Text c="dimmed">No movements found.</Text>}
      {data && data.length > 0 && (
        <SimpleGrid cols={{ base: 1, sm: 2, md: 3 }}>
          {data.map((movement) => (
            <Card key={movement.key} withBorder>
              {movement.representativeIllustration && (
                <Card.Section>
                  <Image
                    src={movement.representativeIllustration}
                    alt=""
                    h={160}
                    fit="contain"
                  />
                </Card.Section>
              )}
              <Text fw={700} mt="sm">
                {movement.name}
              </Text>
              <Group gap="xs" mt="xs">
                {movement.tags.map((t) => (
                  <Badge
                    key={t}
                    variant="light"
                    component="button"
                    onClick={() => setFilter('tag', t)}
                  >
                    {t}
                  </Badge>
                ))}
              </Group>
            </Card>
          ))}
        </SimpleGrid>
      )}
    </Stack>
  );
}
