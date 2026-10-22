import {
  ActionIcon,
  Button,
  Card,
  Group,
  Stack,
  Text,
  Textarea,
  TextInput,
} from '@mantine/core';
import { IconArrowDown, IconArrowUp, IconTrash } from '@tabler/icons-react';
import type { MovementStage } from '@/shared/movements';

/** The ordered stages of a movement: each a name and a description of what it looks like. */
export function StageRows({
  value,
  onChange,
  error,
}: {
  value: MovementStage[];
  onChange: (rows: MovementStage[]) => void;
  error?: string;
}) {
  const change = (index: number, change: Partial<MovementStage>) =>
    onChange(
      value.map((row, i) => (i === index ? { ...row, ...change } : row)),
    );
  const move = (from: number, to: number) => {
    const rows = [...value];
    rows.splice(to, 0, rows.splice(from, 1)[0]);
    onChange(rows);
  };

  return (
    <Stack gap="xs">
      <Text fw={500} size="sm">
        Stages
      </Text>
      {value.map((row, index) => (
        <Card key={index} withBorder padding="sm">
          <Group align="flex-start" wrap="nowrap">
            <Text size="sm" pt={6} w={20}>
              {index + 1}.
            </Text>
            <Stack gap="xs" style={{ flex: 1 }}>
              <TextInput
                aria-label={`Stage ${index + 1}, name`}
                placeholder="Name, e.g. Concentric"
                value={row.name}
                onChange={(e) => change(index, { name: e.currentTarget.value })}
              />
              <Textarea
                aria-label={`Stage ${index + 1}, description`}
                placeholder="What it looks like, e.g. chest to floor, elbows tucked, body straight"
                autosize
                minRows={2}
                value={row.description}
                onChange={(e) =>
                  change(index, { description: e.currentTarget.value })
                }
              />
            </Stack>
            <ActionIcon
              variant="default"
              aria-label={`Move stage ${index + 1} up`}
              disabled={index === 0}
              onClick={() => move(index, index - 1)}
            >
              <IconArrowUp size={16} />
            </ActionIcon>
            <ActionIcon
              variant="default"
              aria-label={`Move stage ${index + 1} down`}
              disabled={index === value.length - 1}
              onClick={() => move(index, index + 1)}
            >
              <IconArrowDown size={16} />
            </ActionIcon>
            <ActionIcon
              variant="subtle"
              color="red"
              aria-label={`Remove stage ${index + 1}`}
              onClick={() => onChange(value.filter((_, i) => i !== index))}
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
        onClick={() => onChange([...value, { name: '', description: '' }])}
      >
        Add stage
      </Button>
      {error && (
        <Text size="xs" c="red">
          {error}
        </Text>
      )}
    </Stack>
  );
}
