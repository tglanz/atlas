import { Badge, Button, Group, Image, Stack, Text } from '@mantine/core';
import { useEffect, useRef } from 'react';
import type { GenerationRequest } from '../api';
import {
  useClearIllustrations,
  useGenerations,
  useRetryGeneration,
} from '../queries';

/** An existing movement's current illustrations and the progress of its generation requests. */
export function IllustrationStatus({
  movementKey,
  illustrations,
  onChanged,
}: {
  movementKey: string;
  /** Urls of the movement's Illustrations, in order. */
  illustrations: string[];
  /** Called after the Illustrations changed, so the movement can be reloaded. */
  onChanged: () => void;
}) {
  const clear = useClearIllustrations(movementKey);
  const generations = useGenerations(movementKey);
  const retry = useRetryGeneration(movementKey);

  // Reload the movement whenever another request has finished successfully.
  const succeeded =
    generations.data?.filter((r) => r.status === 'SUCCEEDED').length ?? 0;
  const seen = useRef<number | null>(null);
  useEffect(() => {
    if (seen.current !== null && succeeded > seen.current) onChanged();
    seen.current = succeeded;
  }, [succeeded, onChanged]);

  return (
    <Stack gap="sm">
      {illustrations.length === 0 ? (
        <Text c="dimmed">No illustrations yet.</Text>
      ) : (
        <Stack gap="xs">
          <Group gap="sm">
            {illustrations.map((url, index) => (
              <Image
                key={index}
                src={url}
                alt={`Step ${index + 1}`}
                h={90}
                w="auto"
                fit="contain"
              />
            ))}
          </Group>
          <Button
            size="compact-xs"
            variant="subtle"
            color="red"
            w="fit-content"
            loading={clear.isPending}
            onClick={() => clear.mutate(undefined, { onSuccess: onChanged })}
          >
            Remove all
          </Button>
        </Stack>
      )}
      {generations.data && generations.data.length > 0 && (
        <Stack gap="xs">
          <Text fw={600}>Requests</Text>
          {generations.data.map((r) => (
            <RequestRow
              key={r.id}
              request={r}
              onRetry={() => retry.mutate(r.id)}
            />
          ))}
        </Stack>
      )}
    </Stack>
  );
}

const statusColor = {
  PENDING: 'gray',
  RUNNING: 'blue',
  SUCCEEDED: 'green',
  FAILED: 'red',
} as const;

function describeStages(request: GenerationRequest): string {
  const generated = request.stages.filter((s) => s.avatar).length;
  const fixed = request.stages.length - generated;
  const parts = [];
  if (generated) parts.push(`${generated} generated`);
  if (fixed) parts.push(`${fixed} static`);
  return parts.join(', ');
}

function RequestRow({
  request,
  onRetry,
}: {
  request: GenerationRequest;
  onRetry: () => void;
}) {
  return (
    <Group gap="sm">
      <Badge color={statusColor[request.status]}>
        {request.status.toLowerCase()}
      </Badge>
      <Text size="sm">
        {new Date(request.createdAt).toLocaleTimeString()}:{' '}
        {describeStages(request)}
      </Text>
      {request.failureReason && (
        <Text size="sm" c="red">
          {request.failureReason}
        </Text>
      )}
      {request.status === 'FAILED' && (
        <Button size="compact-xs" variant="light" onClick={onRetry}>
          Retry
        </Button>
      )}
    </Group>
  );
}
