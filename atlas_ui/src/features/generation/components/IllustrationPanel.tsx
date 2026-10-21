import {
  Alert,
  Badge,
  Button,
  Card,
  FileInput,
  Group,
  Image,
  Select,
  Stack,
  Text,
  Textarea,
  Title,
} from '@mantine/core';
import { useEffect, useRef, useState } from 'react';
import { useAvatars } from '@/features/avatars';
import type { Avatar } from '@/features/avatars';
import { QueryBoundary } from '@/shared/ui';
import type { GenerationRequest, Stage } from '../api';
import {
  useClearIllustrations,
  useGenerations,
  useRequestGeneration,
  useRetryGeneration,
  useUploadIllustration,
  useViewpoints,
} from '../queries';

type Props = {
  movementKey: string;
  /** The draft's Illustrations by Avatar id. */
  illustrations: Record<string, { url: string }[]>;
  /** Called after Illustrations of the draft changed, so the draft can be reloaded. */
  onChanged: () => void;
};

/** Maintainer tools to add Illustrations to a draft: upload a picture or generate a sequence. */
export function IllustrationPanel({
  movementKey,
  illustrations,
  onChanged,
}: Props) {
  const avatars = useAvatars();
  return (
    <Stack gap="lg">
      <Title order={2}>Illustrations</Title>
      <QueryBoundary query={avatars}>
        {(items) =>
          items.length === 0 ? (
            <Alert color="blue">
              Create an avatar in Illustration settings first.
            </Alert>
          ) : (
            <>
              <Sequences
                movementKey={movementKey}
                avatars={items}
                illustrations={illustrations}
                onChanged={onChanged}
              />
              <Upload
                movementKey={movementKey}
                avatars={items}
                onChanged={onChanged}
              />
              <Generate
                movementKey={movementKey}
                avatars={items}
                onChanged={onChanged}
              />
            </>
          )
        }
      </QueryBoundary>
    </Stack>
  );
}

function Sequences({
  movementKey,
  avatars,
  illustrations,
  onChanged,
}: Props & { avatars: Avatar[] }) {
  const clear = useClearIllustrations(movementKey);
  const entries = Object.entries(illustrations);
  if (entries.length === 0)
    return <Text c="dimmed">No illustrations on this draft yet.</Text>;

  return (
    <Stack gap="sm">
      {entries.map(([avatarId, sequence]) => (
        <Card key={avatarId} withBorder padding="sm">
          <Group justify="space-between" mb="xs">
            <Text fw={600}>
              {avatars.find((a) => a.id === avatarId)?.name ?? 'Unknown avatar'}
            </Text>
            <Button
              size="compact-xs"
              variant="subtle"
              color="red"
              loading={clear.isPending}
              onClick={() => clear.mutate(avatarId, { onSuccess: onChanged })}
            >
              Remove all
            </Button>
          </Group>
          <Group gap="sm">
            {sequence.map((illustration, index) => (
              <Image
                key={index}
                src={illustration.url}
                alt={`Step ${index + 1}`}
                h={90}
                w="auto"
                fit="contain"
              />
            ))}
          </Group>
        </Card>
      ))}
    </Stack>
  );
}

const avatarOptions = (avatars: Avatar[]) =>
  avatars.map((a) => ({
    value: a.id,
    label: a.isDefault ? `${a.name} (default)` : a.name,
  }));

function Upload({
  movementKey,
  avatars,
  onChanged,
}: Pick<Props, 'movementKey' | 'onChanged'> & { avatars: Avatar[] }) {
  const upload = useUploadIllustration(movementKey);
  const [avatarId, setAvatarId] = useState<string | null>(null);
  const [file, setFile] = useState<File | null>(null);

  return (
    <Stack gap="xs">
      <Title order={3} size="h4">
        Upload a picture
      </Title>
      <Group align="flex-end">
        <Select
          label="Avatar"
          data={avatarOptions(avatars)}
          value={avatarId}
          onChange={setAvatarId}
        />
        <FileInput
          label="Picture"
          accept="image/png,image/jpeg,image/webp"
          value={file}
          onChange={setFile}
        />
        <Button
          disabled={!avatarId || !file}
          loading={upload.isPending}
          onClick={() =>
            avatarId &&
            file &&
            upload.mutate(
              { avatarId, file },
              {
                onSuccess: () => {
                  setFile(null);
                  onChanged();
                },
              },
            )
          }
        >
          Upload
        </Button>
      </Group>
      {upload.isError && <Alert color="red">{upload.error.message}</Alert>}
    </Stack>
  );
}

/** One line per stage. A line that is a web address is a reference picture; others are words. */
function parseStages(text: string): Stage[] {
  return text
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) =>
      /^https?:\/\//.test(line)
        ? { description: '', referenceUrl: line }
        : { description: line, referenceUrl: '' },
    );
}

function Generate({
  movementKey,
  avatars,
  onChanged,
}: Pick<Props, 'movementKey' | 'onChanged'> & { avatars: Avatar[] }) {
  const viewpoints = useViewpoints();
  const generations = useGenerations(movementKey);
  const request = useRequestGeneration(movementKey);
  const retry = useRetryGeneration(movementKey);
  const [avatarId, setAvatarId] = useState<string | null>(null);
  const [viewpointId, setViewpointId] = useState<string | null>(null);
  const [stages, setStages] = useState('');

  // Reload the draft whenever another request has finished successfully.
  const succeeded =
    generations.data?.filter((r) => r.status === 'SUCCEEDED').length ?? 0;
  const seen = useRef<number | null>(null);
  useEffect(() => {
    if (seen.current !== null && succeeded > seen.current) onChanged();
    seen.current = succeeded;
  }, [succeeded, onChanged]);

  const parsed = parseStages(stages);

  return (
    <Stack gap="xs">
      <Title order={3} size="h4">
        Generate a sequence
      </Title>
      <Text size="sm" c="dimmed">
        Replaces the avatar&apos;s current illustrations on this draft.
      </Text>
      <Group align="flex-end">
        <Select
          label="Avatar"
          data={avatarOptions(avatars)}
          value={avatarId}
          onChange={setAvatarId}
        />
        <Select
          label="Viewpoint"
          data={(viewpoints.data ?? []).map((v) => ({
            value: v.id,
            label: v.name,
          }))}
          value={viewpointId}
          onChange={setViewpointId}
          placeholder={
            viewpoints.data?.length === 0
              ? 'Create a viewpoint first'
              : undefined
          }
        />
      </Group>
      <Textarea
        label="Stages"
        description="One per line, in order: describe the stage in words, or paste a reference picture address."
        autosize
        minRows={3}
        value={stages}
        onChange={(e) => setStages(e.currentTarget.value)}
      />
      <Button
        w="fit-content"
        disabled={!avatarId || !viewpointId || parsed.length === 0}
        loading={request.isPending}
        onClick={() =>
          avatarId &&
          viewpointId &&
          request.mutate(
            { avatarId, viewpointId, stages: parsed },
            { onSuccess: () => setStages('') },
          )
        }
      >
        Generate
      </Button>
      {request.isError && <Alert color="red">{request.error.message}</Alert>}

      {generations.data && generations.data.length > 0 && (
        <Stack gap="xs" mt="sm">
          <Text fw={600}>Requests</Text>
          {generations.data.map((r) => (
            <RequestRow
              key={r.id}
              request={r}
              avatars={avatars}
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

function RequestRow({
  request,
  avatars,
  onRetry,
}: {
  request: GenerationRequest;
  avatars: Avatar[];
  onRetry: () => void;
}) {
  return (
    <Group gap="sm">
      <Badge color={statusColor[request.status]}>
        {request.status.toLowerCase()}
      </Badge>
      <Text size="sm">
        {avatars.find((a) => a.id === request.avatarId)?.name ??
          'Unknown avatar'}
        , {request.stages.length} stage{request.stages.length === 1 ? '' : 's'},{' '}
        {new Date(request.createdAt).toLocaleTimeString()}
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
