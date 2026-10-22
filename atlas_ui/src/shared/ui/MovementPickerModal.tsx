import {
  Button,
  Group,
  Loader,
  Modal,
  Stack,
  Text,
  TextInput,
} from '@mantine/core';
import { useDebouncedValue } from '@mantine/hooks';
import { useState } from 'react';
import { useMovements } from '../movements';

/** A modal with a searchable list of movements; picking one reports its key and closes. */
export function MovementPickerModal({
  opened,
  onClose,
  onPick,
}: {
  opened: boolean;
  onClose: () => void;
  onPick: (movementKey: string) => void;
}) {
  const [text, setText] = useState('');
  // The box updates at once; the request waits until typing pauses.
  const [searchText] = useDebouncedValue(text, 300);
  const movements = useMovements({ text: searchText });

  return (
    <Modal opened={opened} onClose={onClose} title="Add a movement">
      <Stack>
        <TextInput
          label="Search by name"
          data-autofocus
          value={text}
          onChange={(e) => setText(e.currentTarget.value)}
        />
        {movements.isPending && <Loader size="sm" aria-label="Loading" />}
        {movements.isError && <Text c="red">{movements.error.message}</Text>}
        {movements.data?.length === 0 && (
          <Text c="dimmed">No movements found.</Text>
        )}
        {movements.data?.map((movement) => (
          <Group key={movement.key} justify="space-between" wrap="nowrap">
            <Text>{movement.name}</Text>
            <Button
              size="compact-sm"
              variant="light"
              onClick={() => {
                onPick(movement.key);
                onClose();
              }}
            >
              Add
            </Button>
          </Group>
        ))}
      </Stack>
    </Modal>
  );
}
