import {
  Alert,
  Autocomplete,
  Button,
  Chip,
  Grid,
  Group,
  Loader,
  Paper,
  SimpleGrid,
  Stack,
  Text,
  Title,
} from '@mantine/core';
import { useDebouncedValue } from '@mantine/hooks';
import { IconSearch } from '@tabler/icons-react';
import { Link, useSearchParams } from 'react-router';
import { useCurrentUser } from '@/features/auth';
import {
  useMovementNames,
  useMovementTags,
  useMovements,
} from '@/shared/movements';
import { MovementCard, formatTag } from '@/shared/ui';

const SEARCH_DELAY_MS = 300;
const MAX_SUGGESTIONS = 8;

/**
 * The landing page of the Movements section: find movements by name, with suggestions while typing,
 * and by toggling tags. Both filters live in the address, so a search can be shared.
 */
export function MovementIndexPage() {
  const [params, setParams] = useSearchParams();
  const text = params.get('text') ?? '';
  const tags = params.getAll('tags');
  const user = useCurrentUser();

  // The box updates at once; the request waits until typing pauses.
  const [searchText] = useDebouncedValue(text, SEARCH_DELAY_MS);
  const movements = useMovements({ text: searchText, tags });
  const names = useMovementNames();
  const allTags = useMovementTags();

  function setText(value: string) {
    const next = new URLSearchParams(params);
    if (value) next.set('text', value);
    else next.delete('text');
    setParams(next, { replace: true });
  }

  function setTags(selected: string[]) {
    const next = new URLSearchParams(params);
    next.delete('tags');
    selected.forEach((tag) => next.append('tags', tag));
    setParams(next, { replace: true });
  }

  return (
    <Stack gap="lg">
      <Group justify="space-between">
        <Title order={1}>Movement index</Title>
        {user.data?.role === 'MAINTAINER' && (
          <Button component={Link} to="/movements/new">
            New movement
          </Button>
        )}
      </Group>

      <Autocomplete
        aria-label="Search by name"
        placeholder="Search by name"
        leftSection={<IconSearch size={16} />}
        data={[...new Set((names.data ?? []).map((n) => n.name))]}
        limit={MAX_SUGGESTIONS}
        value={text}
        onChange={setText}
      />

      <Grid gap="lg">
        <Grid.Col span={{ base: 12, md: 3 }}>
          <Paper withBorder p="md">
            <Stack gap="sm">
              <Text fw={600}>Tags</Text>
              {allTags.isPending && <Loader size="sm" aria-label="Loading" />}
              {allTags.isError && (
                <Text c="red" size="sm">
                  {allTags.error.message}
                </Text>
              )}
              {allTags.data && (
                <Chip.Group multiple value={tags} onChange={setTags}>
                  <Group gap="xs">
                    {allTags.data.map((t) => (
                      <Chip key={t.tag} value={t.tag} size="sm">
                        {formatTag(t.tag)} ({t.count})
                      </Chip>
                    ))}
                  </Group>
                </Chip.Group>
              )}
              {(text || tags.length > 0) && (
                <Button
                  variant="subtle"
                  size="compact-sm"
                  w="fit-content"
                  onClick={() => setParams({}, { replace: true })}
                >
                  Clear filters
                </Button>
              )}
              {tags.length > 1 && (
                <Text size="xs" c="dimmed">
                  Movements with all of the selected tags.
                </Text>
              )}
            </Stack>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, md: 9 }}>
          <Stack gap="md">
            {movements.isPending && <Loader aria-label="Loading movements" />}
            {movements.isError && (
              <Alert color="red" title="Could not load movements">
                {movements.error.message}
              </Alert>
            )}
            {movements.data && (
              <Text size="sm" c="dimmed">
                {movements.data.length === 0
                  ? 'No movements found.'
                  : `${movements.data.length} movement${movements.data.length === 1 ? '' : 's'}`}
              </Text>
            )}
            {movements.data && movements.data.length > 0 && (
              <SimpleGrid cols={{ base: 1, sm: 2, lg: 3 }}>
                {movements.data.map((movement) => (
                  <MovementCard key={movement.key} movement={movement} />
                ))}
              </SimpleGrid>
            )}
          </Stack>
        </Grid.Col>
      </Grid>
    </Stack>
  );
}
