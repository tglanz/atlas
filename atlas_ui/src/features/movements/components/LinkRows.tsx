import {
  ActionIcon,
  Button,
  Group,
  Loader,
  Select,
  Stack,
  Text,
} from '@mantine/core';
import { IconTrash } from '@tabler/icons-react';
import { useEdgeTypes } from '@/features/graph';
import { MovementSelect } from '@/shared/ui';

export type LinkRow = { target: string; type: string };

/** The relationships leaving a movement: each is a target movement and a type from the offered list. */
export function LinkRows({
  value,
  onChange,
  error,
}: {
  value: LinkRow[];
  onChange: (rows: LinkRow[]) => void;
  error?: string;
}) {
  const types = useEdgeTypes();
  if (types.isPending) return <Loader size="sm" aria-label="Loading" />;

  // Offer the known types, plus any other type an existing relationship already uses.
  const known = (types.data ?? []).map((t) => ({
    value: t.name,
    label: t.label,
  }));
  const extra = [...new Set(value.map((row) => row.type))]
    .filter((type) => type && !known.some((k) => k.value === type))
    .map((type) => ({ value: type, label: type }));
  const options = [...known, ...extra];

  const change = (index: number, change: Partial<LinkRow>) =>
    onChange(
      value.map((row, i) => (i === index ? { ...row, ...change } : row)),
    );

  return (
    <Stack gap="xs">
      <Text fw={500} size="sm">
        Relationships
      </Text>
      {value.map((row, index) => (
        <Group key={index} align="flex-start" wrap="nowrap">
          <div style={{ flex: 2 }}>
            <MovementSelect
              ariaLabel={`Relationship ${index + 1}, movement`}
              value={row.target}
              onChange={(target) => change(index, { target })}
            />
          </div>
          <Select
            style={{ flex: 1 }}
            aria-label={`Relationship ${index + 1}, type`}
            placeholder="Type"
            data={options}
            value={row.type || null}
            onChange={(type) => change(index, { type: type ?? '' })}
          />
          <ActionIcon
            variant="subtle"
            color="red"
            aria-label={`Remove relationship ${index + 1}`}
            onClick={() => onChange(value.filter((_, i) => i !== index))}
          >
            <IconTrash size={16} />
          </ActionIcon>
        </Group>
      ))}
      <Button
        variant="default"
        size="xs"
        w="fit-content"
        onClick={() => onChange([...value, { target: '', type: '' }])}
      >
        Add relationship
      </Button>
      {error && (
        <Text size="xs" c="red">
          {error}
        </Text>
      )}
    </Stack>
  );
}
