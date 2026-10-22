import {
  ActionIcon,
  Alert,
  Button,
  Card,
  FileInput,
  Group,
  SegmentedControl,
  Select,
  Stack,
  Text,
  Textarea,
} from '@mantine/core';
import { IconTrash } from '@tabler/icons-react';
import { QueryBoundary } from '@/shared/ui';
import type { StageInput } from '../queries';
import type { IllustrationInputState, Mode } from '../useIllustrationInput';
import { emptyStage } from '../useIllustrationInput';

const IMAGE_TYPES = 'image/png,image/jpeg,image/webp';

/** The fields for defining a movement's illustrations. The owner decides when to submit them. */
export function IllustrationInput({
  input,
}: {
  input: IllustrationInputState;
}) {
  const { mode, stages, setStages } = input;

  function changeStage(index: number, change: Partial<StageInput>) {
    setStages((all) =>
      all.map((s, i) => (i === index ? { ...s, ...change } : s)),
    );
  }

  return (
    <Stack gap="sm">
      <SegmentedControl
        w="fit-content"
        value={mode}
        onChange={(value) => input.setMode(value as Mode)}
        data={[
          { value: 'avatar', label: 'Generate with an avatar' },
          { value: 'static', label: 'Use my own images' },
        ]}
      />

      {mode === 'avatar' && (
        <QueryBoundary query={input.avatars}>
          {(avatarItems) => (
            <QueryBoundary query={input.viewpoints}>
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
                      value={input.chosenAvatarId}
                      onChange={input.setAvatarId}
                    />
                    <Select
                      label="Viewpoint"
                      data={viewpointItems.map((v) => ({
                        value: v.id,
                        label: v.name,
                      }))}
                      value={input.chosenViewpointId}
                      onChange={input.setViewpointId}
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
      <Button
        variant="default"
        size="xs"
        w="fit-content"
        disabled={stages.length >= 20}
        onClick={() => setStages((all) => [...all, emptyStage()])}
      >
        Add stage
      </Button>
      {input.problem && <Alert color="yellow">{input.problem}</Alert>}
    </Stack>
  );
}
