import {
  Alert,
  Button,
  Card,
  Group,
  Modal,
  Stack,
  Text,
  Textarea,
  TextInput,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { useState } from 'react';
import type { ReactNode } from 'react';

type Item = { id: string; name: string; specification?: string };

export type SpecificationValues = { name: string; specification: string };

/** A simple list with add, edit and delete for things that have a name and a specification. */
export function SpecificationManager<T extends Item>({
  noun,
  items,
  onSave,
  onDelete,
  extra,
}: {
  noun: string;
  items: T[];
  /** Creates when id is undefined, otherwise updates. Rejects with an Error to show a message. */
  onSave: (
    id: string | undefined,
    values: SpecificationValues,
  ) => Promise<unknown>;
  onDelete: (id: string) => Promise<unknown>;
  /** Extra content shown beside each item, such as a badge or a button. */
  extra?: (item: T) => ReactNode;
}) {
  const [editing, setEditing] = useState<T | 'new' | null>(null);
  const [deleting, setDeleting] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const form = useForm<SpecificationValues>({
    initialValues: { name: '', specification: '' },
    validate: {
      name: (v) => (v.trim() ? null : 'Enter a name'),
      specification: (v) => (v.trim() ? null : 'Enter a specification'),
    },
  });

  function open(target: T | 'new') {
    setError(null);
    form.setValues({
      name: target === 'new' ? '' : target.name,
      specification: target === 'new' ? '' : (target.specification ?? ''),
    });
    form.resetDirty();
    setEditing(target);
  }

  async function run(action: () => Promise<unknown>, done: () => void) {
    setBusy(true);
    setError(null);
    try {
      await action();
      done();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong');
    } finally {
      setBusy(false);
    }
  }

  return (
    <Stack gap="md">
      <Group>
        <Button size="xs" onClick={() => open('new')}>
          Add {noun}
        </Button>
      </Group>
      {error && !editing && !deleting && <Alert color="red">{error}</Alert>}
      {items.length === 0 && <Text c="dimmed">No {noun}s yet.</Text>}
      {items.map((item) => (
        <Card key={item.id} withBorder padding="sm">
          <Group justify="space-between" wrap="nowrap">
            <Group gap="sm">
              <Text fw={600}>{item.name}</Text>
              {extra?.(item)}
            </Group>
            <Group gap="xs" wrap="nowrap">
              <Button size="xs" variant="default" onClick={() => open(item)}>
                Edit
              </Button>
              <Button
                size="xs"
                variant="default"
                color="red"
                onClick={() => {
                  setError(null);
                  setDeleting(item);
                }}
              >
                Delete
              </Button>
            </Group>
          </Group>
        </Card>
      ))}

      <Modal
        opened={editing !== null}
        onClose={() => setEditing(null)}
        title={editing === 'new' ? `Add ${noun}` : `Edit ${noun}`}
      >
        <form
          onSubmit={form.onSubmit((values) =>
            run(
              () =>
                onSave(
                  editing === 'new' || editing === null
                    ? undefined
                    : editing.id,
                  values,
                ),
              () => setEditing(null),
            ),
          )}
        >
          <Stack>
            <TextInput
              label="Name"
              data-autofocus
              {...form.getInputProps('name')}
            />
            <Textarea
              label="Specification"
              autosize
              minRows={4}
              {...form.getInputProps('specification')}
            />
            {error && <Alert color="red">{error}</Alert>}
            <Group justify="flex-end">
              <Button variant="default" onClick={() => setEditing(null)}>
                Cancel
              </Button>
              <Button type="submit" loading={busy}>
                Save
              </Button>
            </Group>
          </Stack>
        </form>
      </Modal>

      <Modal
        opened={deleting !== null}
        onClose={() => setDeleting(null)}
        title={`Delete ${noun}?`}
      >
        <Stack>
          <Text>Delete {deleting?.name}? This cannot be undone.</Text>
          {error && <Alert color="red">{error}</Alert>}
          <Group justify="flex-end">
            <Button variant="default" onClick={() => setDeleting(null)}>
              Cancel
            </Button>
            <Button
              color="red"
              loading={busy}
              onClick={() =>
                deleting &&
                run(
                  () => onDelete(deleting.id),
                  () => setDeleting(null),
                )
              }
            >
              Delete
            </Button>
          </Group>
        </Stack>
      </Modal>
    </Stack>
  );
}
