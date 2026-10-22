import {
  ActionIcon,
  Alert,
  Button,
  Card,
  Group,
  Stack,
  Text,
  Textarea,
  TextInput,
  Title,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconArrowDown, IconArrowUp, IconTrash } from '@tabler/icons-react';
import { useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { MovementPickerModal } from '@/features/movements';
import { QueryBoundary } from '@/shared/ui';
import type { RoutineInput } from '../api';
import { MovementRefName } from '../components/MovementRefName';
import { useCreateRoutine, useRoutine, useUpdateRoutine } from '../queries';

/** /routines/new creates a routine; /routines/:id/edit changes one. */
export function RoutineEditorPage() {
  const { id } = useParams();
  return id ? <EditRoutine id={id} /> : <RoutineForm />;
}

function EditRoutine({ id }: { id: string }) {
  const routine = useRoutine(id);
  return (
    <QueryBoundary query={routine}>
      {(data) => <RoutineForm id={id} initial={data} />}
    </QueryBoundary>
  );
}

function RoutineForm({ id, initial }: { id?: string; initial?: RoutineInput }) {
  const navigate = useNavigate();
  const create = useCreateRoutine();
  const update = useUpdateRoutine();
  const [pickerSection, setPickerSection] = useState<number | null>(null);

  const form = useForm<RoutineInput>({
    initialValues: initial ?? { name: '', description: '', sections: [] },
    validate: {
      name: (v) => (v.trim() ? null : 'Enter a name'),
      sections: { name: (v) => (v.trim() ? null : 'Enter a section name') },
    },
  });

  const error = create.error ?? update.error;

  return (
    <form
      onSubmit={form.onSubmit(async (values) => {
        const saved = id
          ? await update.mutateAsync({ id, input: values })
          : await create.mutateAsync(values);
        navigate(`/routines/${(saved as { id: string }).id}`);
      })}
    >
      <Stack gap="md">
        <Title order={1}>{id ? 'Edit routine' : 'New routine'}</Title>
        <TextInput label="Name" {...form.getInputProps('name')} />
        <Textarea
          label="Description"
          autosize
          minRows={2}
          {...form.getInputProps('description')}
        />

        {form.values.sections.map((section, index) => (
          <Card key={index} withBorder padding="sm">
            <Stack gap="xs">
              <Group align="flex-start" wrap="nowrap">
                <TextInput
                  aria-label="Section name"
                  placeholder="Section name"
                  style={{ flex: 1 }}
                  {...form.getInputProps(`sections.${index}.name`)}
                />
                <ActionIcon
                  variant="default"
                  aria-label="Move section up"
                  disabled={index === 0}
                  onClick={() =>
                    form.reorderListItem('sections', {
                      from: index,
                      to: index - 1,
                    })
                  }
                >
                  <IconArrowUp size={16} />
                </ActionIcon>
                <ActionIcon
                  variant="default"
                  aria-label="Move section down"
                  disabled={index === form.values.sections.length - 1}
                  onClick={() =>
                    form.reorderListItem('sections', {
                      from: index,
                      to: index + 1,
                    })
                  }
                >
                  <IconArrowDown size={16} />
                </ActionIcon>
                <ActionIcon
                  variant="subtle"
                  color="red"
                  aria-label="Remove section"
                  onClick={() => form.removeListItem('sections', index)}
                >
                  <IconTrash size={16} />
                </ActionIcon>
              </Group>
              <Textarea
                aria-label="Section description"
                placeholder="Description (optional)"
                autosize
                {...form.getInputProps(`sections.${index}.description`)}
              />

              {section.movements.length === 0 && (
                <Text c="dimmed" size="sm">
                  No movements yet.
                </Text>
              )}
              {section.movements.map((movement, i) => (
                <Group
                  key={`${movement}-${i}`}
                  justify="space-between"
                  wrap="nowrap"
                >
                  <Text size="sm">
                    {i + 1}.{' '}
                    <MovementRefName movementKey={movement} link={false} />
                  </Text>
                  <Group gap={4} wrap="nowrap">
                    <ActionIcon
                      variant="default"
                      aria-label="Move movement up"
                      disabled={i === 0}
                      onClick={() =>
                        form.reorderListItem(`sections.${index}.movements`, {
                          from: i,
                          to: i - 1,
                        })
                      }
                    >
                      <IconArrowUp size={14} />
                    </ActionIcon>
                    <ActionIcon
                      variant="default"
                      aria-label="Move movement down"
                      disabled={i === section.movements.length - 1}
                      onClick={() =>
                        form.reorderListItem(`sections.${index}.movements`, {
                          from: i,
                          to: i + 1,
                        })
                      }
                    >
                      <IconArrowDown size={14} />
                    </ActionIcon>
                    <ActionIcon
                      variant="subtle"
                      color="red"
                      aria-label="Remove movement"
                      onClick={() =>
                        form.removeListItem(`sections.${index}.movements`, i)
                      }
                    >
                      <IconTrash size={14} />
                    </ActionIcon>
                  </Group>
                </Group>
              ))}
              <Button
                size="xs"
                variant="light"
                w="fit-content"
                onClick={() => setPickerSection(index)}
              >
                Add movement
              </Button>
            </Stack>
          </Card>
        ))}

        <Button
          variant="default"
          w="fit-content"
          onClick={() =>
            form.insertListItem('sections', {
              name: '',
              description: '',
              movements: [],
            })
          }
        >
          Add section
        </Button>

        {error && <Alert color="red">{error.message}</Alert>}
        <Group>
          <Button type="submit" loading={create.isPending || update.isPending}>
            Save
          </Button>
          <Button variant="default" onClick={() => navigate(-1)}>
            Cancel
          </Button>
        </Group>
      </Stack>

      <MovementPickerModal
        opened={pickerSection !== null}
        onClose={() => setPickerSection(null)}
        onPick={(movementKey) =>
          pickerSection !== null &&
          form.insertListItem(
            `sections.${pickerSection}.movements`,
            movementKey,
          )
        }
      />
    </form>
  );
}
