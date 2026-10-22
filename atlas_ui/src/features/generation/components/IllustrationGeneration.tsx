import {
  Alert,
  Button,
  Card,
  Group,
  Image,
  List,
  Loader,
  Select,
  Stack,
  Text,
} from '@mantine/core';
import { useState } from 'react';
import { useAvatars } from '@/features/avatars';
import { QueryBoundary } from '@/shared/ui';
import type { MovementStage } from '@/shared/movements';
import type { GenerationMovementIllustrations } from '../api';
import {
  useApplyGeneration,
  useClearIllustrations,
  useDiscardGeneration,
  useGenerations,
  useRequestGeneration,
  useViewpoints,
} from '../queries';

/** An existing movement's illustrations, and generating new ones for review. */
export function IllustrationGeneration({
  movementKey,
  illustrations,
  stages,
  onChanged,
}: {
  movementKey: string;
  /** Image urls of the movement's Illustrations, in order. */
  illustrations: string[];
  /** The movement's stages: one image is generated for each. */
  stages: MovementStage[];
  /** Called after the Illustrations changed, so the movement can be reloaded. */
  onChanged: () => void;
}) {
  const clear = useClearIllustrations(movementKey);
  const generations = useGenerations(movementKey);
  const latest = generations.data?.[0];
  const inProgress =
    latest?.status === 'PENDING' || latest?.status === 'RUNNING';
  const reviewable =
    latest &&
    (inProgress ||
      latest.status === 'READY_FOR_REVIEW' ||
      latest.status === 'FAILED');

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
      {reviewable && (
        <Review
          movementKey={movementKey}
          generation={latest}
          onApplied={onChanged}
        />
      )}
      <GenerateForm
        movementKey={movementKey}
        stages={stages}
        disabled={inProgress}
      />
    </Stack>
  );
}

function GenerateForm({
  movementKey,
  stages,
  disabled,
}: {
  movementKey: string;
  stages: MovementStage[];
  disabled: boolean;
}) {
  const avatars = useAvatars();
  const viewpoints = useViewpoints();
  const request = useRequestGeneration(movementKey);
  const [avatarId, setAvatarId] = useState<string | null>(null);
  const [viewpointId, setViewpointId] = useState<string | null>(null);

  if (stages.length === 0) {
    return (
      <Alert color="blue">
        Add stages to this movement and save it to generate its illustrations.
      </Alert>
    );
  }

  return (
    <QueryBoundary query={avatars}>
      {(avatarItems) => (
        <QueryBoundary query={viewpoints}>
          {(viewpointItems) => {
            if (avatarItems.length === 0 || viewpointItems.length === 0) {
              return (
                <Alert color="blue">
                  Create at least one avatar and one viewpoint in Illustration
                  settings first.
                </Alert>
              );
            }
            // Preselect the default viewpoint; a single avatar needs no choosing.
            const chosenAvatar =
              avatarId ?? (avatarItems.length === 1 ? avatarItems[0].id : null);
            const chosenViewpoint =
              viewpointId ??
              viewpointItems.find((v) => v.isDefault)?.id ??
              (viewpointItems.length === 1 ? viewpointItems[0].id : null);
            const ready = Boolean(chosenAvatar && chosenViewpoint);

            return (
              <Stack gap="sm">
                <Group>
                  <Select
                    label="Avatar"
                    data={avatarItems.map((a) => ({
                      value: a.id,
                      label: a.name,
                    }))}
                    value={chosenAvatar}
                    onChange={setAvatarId}
                  />
                  <Select
                    label="Viewpoint"
                    data={viewpointItems.map((v) => ({
                      value: v.id,
                      label: v.name,
                    }))}
                    value={chosenViewpoint}
                    onChange={setViewpointId}
                  />
                </Group>
                <Text size="sm" c="dimmed">
                  One image is generated for each stage of the movement. Edit
                  the stages above and save to change them.
                </Text>
                <List type="ordered" size="sm">
                  {stages.map((stage, index) => (
                    <List.Item key={index}>
                      <Text span fw={600} size="sm">
                        {stage.name}
                      </Text>
                      : {stage.description}
                    </List.Item>
                  ))}
                </List>
                <Group>
                  <Button
                    size="xs"
                    loading={request.isPending}
                    disabled={disabled || !ready}
                    onClick={() =>
                      request.mutate({
                        avatarId: chosenAvatar as string,
                        viewpointId: chosenViewpoint as string,
                      })
                    }
                  >
                    Generate
                  </Button>
                </Group>
                {disabled && (
                  <Text size="sm" c="dimmed">
                    A generation is in progress.
                  </Text>
                )}
                {request.error && (
                  <Alert color="red">{request.error.message}</Alert>
                )}
              </Stack>
            );
          }}
        </QueryBoundary>
      )}
    </QueryBoundary>
  );
}

/** The latest generation: a spinner or image for every stage, and approval once all are done. */
function Review({
  movementKey,
  generation,
  onApplied,
}: {
  movementKey: string;
  generation: GenerationMovementIllustrations;
  onApplied: () => void;
}) {
  const apply = useApplyGeneration(movementKey);
  const discard = useDiscardGeneration(movementKey);
  const error = apply.error ?? discard.error;

  return (
    <Card withBorder padding="sm">
      <Stack gap="sm">
        <Text fw={600}>New illustrations</Text>
        <Group gap="sm" align="flex-start">
          {generation.stages.map((stage) => (
            <Stack key={stage.position} gap={4} align="center">
              {stage.candidate.state === 'DONE' && stage.candidate.imageUrl ? (
                <Image
                  src={stage.candidate.imageUrl}
                  alt={`New ${stage.name}`}
                  h={120}
                  w="auto"
                  fit="contain"
                />
              ) : stage.candidate.state === 'FAILED' ? (
                <Text size="sm" c="red" maw={160}>
                  {stage.candidate.failureReason ?? 'Failed'}
                </Text>
              ) : generation.status === 'FAILED' ? (
                <Text size="sm" c="dimmed">
                  Not generated
                </Text>
              ) : (
                <Loader size="sm" aria-label={`Generating ${stage.name}`} />
              )}
              {stage.candidate.state === 'PENDING' &&
                stage.candidate.progress && (
                  <Text size="xs" c="dimmed" maw={160} ta="center">
                    {stage.candidate.progress}
                  </Text>
                )}
              <Text size="xs" c="dimmed">
                {stage.name}
              </Text>
            </Stack>
          ))}
        </Group>
        {generation.status === 'FAILED' && (
          <Alert color="red">{generation.failureReason}</Alert>
        )}
        {generation.status === 'READY_FOR_REVIEW' && (
          <Group>
            <Button
              size="xs"
              loading={apply.isPending}
              disabled={discard.isPending}
              onClick={() =>
                apply.mutate(generation.id, { onSuccess: onApplied })
              }
            >
              Approve
            </Button>
            <Button
              size="xs"
              variant="default"
              loading={discard.isPending}
              disabled={apply.isPending}
              onClick={() => discard.mutate(generation.id)}
            >
              Discard
            </Button>
          </Group>
        )}
        {error && <Alert color="red">{error.message}</Alert>}
      </Stack>
    </Card>
  );
}
