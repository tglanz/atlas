import {
  ActionIcon,
  Alert,
  Badge,
  Button,
  Card,
  FileInput,
  Group,
  Image,
  Select,
  SegmentedControl,
  Stack,
  Text,
  Textarea,
  Title,
} from '@mantine/core';
import { IconTrash } from '@tabler/icons-react';
import { useEffect, useRef, useState } from 'react';
import { useAvatars } from '@/features/avatars';
import { QueryBoundary } from '@/shared/ui';
import type { GenerationRequest } from '../api';
import type { StageDraft } from '../queries';
import {
  useClearIllustrations,
  useGenerations,
  useRequestGeneration,
  useRetryGeneration,
  useViewpoints,
} from '../queries';

const IMAGE_TYPES = 'image/png,image/jpeg,image/webp';

type Mode = 'avatar' | 'static';

type Props = {
  movementKey: string;
  /** Urls of the draft's Illustrations, in order. */
  illustrations: string[];
  /** Called after the draft's Illustrations changed, so the draft can be reloaded. */
  onChanged: () => void;
};

/**
 * Maintainer tools to set a draft's Illustrations. The data model allows any mix of stages; the UI
 * offers one pattern per movement: all stages generated with an avatar, or all static images.
 * Either way the whole sequence is replaced.
 */
export function IllustrationPanel({
  movementKey,
  illustrations,
  onChanged,
}: Props) {
  const clear = useClearIllustrations(movementKey);

  return (
    <Stack gap="lg">
      <Title order={2}>Illustrations</Title>
      {illustrations.length === 0 ? (
        <Text c="dimmed">No illustrations on this draft yet.</Text>
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
      <Producer movementKey={movementKey} onChanged={onChanged} />
    </Stack>
  );
}

const emptyStage = (): StageDraft => ({ instructions: '', file: null });

function Producer({
  movementKey,
  onChanged,
}: Pick<Props, 'movementKey' | 'onChanged'>) {
  const avatars = useAvatars();
  const viewpoints = useViewpoints();
  const generations = useGenerations(movementKey);
  const request = useRequestGeneration(movementKey);
  const retry = useRetryGeneration(movementKey);

  const [mode, setMode] = useState<Mode>('avatar');
  const [avatarId, setAvatarId] = useState<string | null>(null);
  const [viewpointId, setViewpointId] = useState<string | null>(null);
  const [stages, setStages] = useState<StageDraft[]>([emptyStage()]);

  // Reload the draft whenever another request has finished successfully.
  const succeeded =
    generations.data?.filter((r) => r.status === 'SUCCEEDED').length ?? 0;
  const seen = useRef<number | null>(null);
  useEffect(() => {
    if (seen.current !== null && succeeded > seen.current) onChanged();
    seen.current = succeeded;
  }, [succeeded, onChanged]);

  function changeStage(index: number, change: Partial<StageDraft>) {
    setStages((all) =>
      all.map((s, i) => (i === index ? { ...s, ...change } : s)),
    );
  }

  const stagesReady =
    stages.length > 0 &&
    stages.every((s) =>
      mode === 'static' ? s.file !== null : s.instructions.trim() || s.file,
    );
  const canSubmit = Boolean(
    stagesReady && (mode === 'static' || (avatarId && viewpointId)),
  );

  function submit() {
    const form =
      mode === 'static'
        ? { mode, stages }
        : avatarId && viewpointId
          ? { mode, avatarId, viewpointId, stages }
          : null;
    if (!form) return;
    request.mutate(form, { onSuccess: () => setStages([emptyStage()]) });
  }

  return (
    <Stack gap="sm">
      <Title order={3} size="h4">
        Set illustrations
      </Title>
      <Text size="sm" c="dimmed">
        One picture per stage. This replaces the draft&apos;s current
        illustrations.
      </Text>
      <SegmentedControl
        w="fit-content"
        value={mode}
        onChange={(value) => {
          setMode(value as Mode);
          setStages([emptyStage()]);
        }}
        data={[
          { value: 'avatar', label: 'Generate with an avatar' },
          { value: 'static', label: 'Use my own images' },
        ]}
      />

      {mode === 'avatar' && (
        <QueryBoundary query={avatars}>
          {(avatarItems) => (
            <QueryBoundary query={viewpoints}>
              {(viewpointItems) =>
                avatarItems.length === 0 || viewpointItems.length === 0 ? (
                  <Alert color="blue">
                    Create at least one avatar and one viewpoint in Illustration
                    settings first.
                  </Alert>
                ) : (
                  <Group>
                    <Select
                      label="Avatar"
                      data={avatarItems.map((a) => ({
                        value: a.id,
                        label: a.name,
                      }))}
                      value={avatarId}
                      onChange={setAvatarId}
                    />
                    <Select
                      label="Viewpoint"
                      data={viewpointItems.map((v) => ({
                        value: v.id,
                        label: v.name,
                      }))}
                      value={viewpointId}
                      onChange={setViewpointId}
                    />
                  </Group>
                )
              }
            </QueryBoundary>
          )}
        </QueryBoundary>
      )}

      {stages.map((stage, index) => (
        <Card key={index} withBorder padding="sm">
          <Group align="flex-start" wrap="nowrap">
            <Text fw={600} w={60}>
              Stage {index + 1}
            </Text>
            <Stack gap="xs" style={{ flex: 1 }}>
              {mode === 'avatar' && (
                <Textarea
                  aria-label={`Stage ${index + 1} instructions`}
                  placeholder="Instructions, e.g. chest an inch above the floor"
                  autosize
                  minRows={2}
                  value={stage.instructions}
                  onChange={(e) =>
                    changeStage(index, { instructions: e.currentTarget.value })
                  }
                />
              )}
              <FileInput
                aria-label={
                  mode === 'avatar'
                    ? `Stage ${index + 1} reference image`
                    : `Stage ${index + 1} image`
                }
                placeholder={
                  mode === 'avatar'
                    ? 'Reference image (optional if instructions)'
                    : 'Image'
                }
                accept={IMAGE_TYPES}
                clearable
                value={stage.file}
                onChange={(file) => changeStage(index, { file })}
              />
            </Stack>
            <ActionIcon
              variant="subtle"
              color="red"
              aria-label={`Remove stage ${index + 1}`}
              disabled={stages.length === 1}
              onClick={() =>
                setStages((all) => all.filter((_, i) => i !== index))
              }
            >
              <IconTrash size={16} />
            </ActionIcon>
          </Group>
        </Card>
      ))}
      <Group>
        <Button
          variant="default"
          size="xs"
          disabled={stages.length >= 20}
          onClick={() => setStages((all) => [...all, emptyStage()])}
        >
          Add stage
        </Button>
        <Button
          disabled={!canSubmit}
          loading={request.isPending}
          onClick={submit}
        >
          {mode === 'avatar' ? 'Generate' : 'Use these images'}
        </Button>
      </Group>
      {request.isError && <Alert color="red">{request.error.message}</Alert>}

      {generations.data && generations.data.length > 0 && (
        <Stack gap="xs" mt="sm">
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
