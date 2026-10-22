import {
  ActionIcon,
  Alert,
  Button,
  Group,
  Stack,
  TagsInput,
  Text,
  Textarea,
  TextInput,
  Title,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconArrowDown, IconArrowUp, IconTrash } from '@tabler/icons-react';
import type { UseFormReturnType } from '@mantine/form';
import { useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { IllustrationPanel } from '@/features/generation';
import { QueryBoundary } from '@/shared/ui';
import type { Movement, MovementContent, MovementItem } from '../api';
import {
  movementKeys,
  useCreateMovement,
  useMovement,
  useUpdateMovement,
} from '../queries';

/** /movements/new creates a movement; /movements/:key/edit changes one. */
export function MovementEditorPage() {
  const { key } = useParams();
  return key ? <EditMovement movementKey={key} /> : <MovementForm />;
}

function EditMovement({ movementKey }: { movementKey: string }) {
  const movement = useMovement(movementKey);
  const queryClient = useQueryClient();

  return (
    <QueryBoundary query={movement}>
      {(data) => (
        <Stack gap="xl">
          <MovementForm movement={data} />
          <IllustrationPanel
            movementKey={data.key}
            illustrations={data.illustrations}
            onChanged={() =>
              queryClient.invalidateQueries({ queryKey: movementKeys.all })
            }
          />
        </Stack>
      )}
    </QueryBoundary>
  );
}

type FormValues = {
  key: string;
  name: string;
  description: string;
  // One list per item type the editor offers; other types are carried through untouched.
  cues: string[];
  comments: string[];
  otherItems: MovementItem[];
  tags: string[];
  links: { target: string; relationType: string }[];
};

const KEY_FORMAT = /^[a-z0-9]+(-[a-z0-9]+)*$/;

/** Tags already added, plus any text still typed in the tag field. */
function withPendingTag(tags: string[], pending: string): string[] {
  const text = pending.trim();
  const exists = tags.some((tag) => tag.toLowerCase() === text.toLowerCase());
  return text && !exists ? [...tags, text] : tags;
}

function toContent(values: FormValues, pendingTag: string): MovementContent {
  const texts = (type: string, list: string[]): MovementItem[] =>
    list
      .map((content) => content.trim())
      .filter(Boolean)
      .map((content) => ({ type, content }));
  return {
    name: values.name,
    description: values.description,
    items: [
      ...texts('CUE', values.cues),
      ...texts('COMMENT', values.comments),
      ...values.otherItems,
    ],
    tags: withPendingTag(values.tags, pendingTag),
    links: values.links.filter((link) => link.target && link.relationType),
  };
}

function contentsOf(movement: Movement | undefined, type: string): string[] {
  return (
    movement?.items.filter((i) => i.type === type).map((i) => i.content) ?? []
  );
}

function MovementForm({ movement }: { movement?: Movement }) {
  const navigate = useNavigate();
  const create = useCreateMovement();
  const update = useUpdateMovement();
  // Text typed in the tag field but not yet turned into a tag with Enter.
  const [pendingTag, setPendingTag] = useState('');

  const form = useForm<FormValues>({
    initialValues: {
      key: movement?.key ?? '',
      name: movement?.name ?? '',
      description: movement?.description ?? '',
      cues: contentsOf(movement, 'CUE'),
      comments: contentsOf(movement, 'COMMENT'),
      otherItems:
        movement?.items.filter(
          (i) => i.type !== 'CUE' && i.type !== 'COMMENT',
        ) ?? [],
      tags: movement?.tags ?? [],
      links: movement?.links ?? [],
    },
    validate: {
      key: (v) =>
        movement || KEY_FORMAT.test(v)
          ? null
          : 'Use lowercase letters, digits and hyphens',
      name: (v) => (v.trim() ? null : 'Enter a name'),
    },
  });

  const busy = create.isPending || update.isPending;
  const error = create.error ?? update.error;

  async function save(values: FormValues) {
    const content = toContent(values, pendingTag);
    if (movement) {
      await update.mutateAsync({ key: movement.key, content });
      navigate(`/movements/${movement.key}`);
    } else {
      await create.mutateAsync({ key: values.key, content });
      // Illustrations are added on the edit page, which a new movement opens next.
      navigate(`/movements/${values.key}/edit`);
    }
  }

  return (
    <form onSubmit={form.onSubmit(save)}>
      <Stack gap="md">
        <Title order={1}>
          {movement ? `Edit ${movement.name}` : 'New movement'}
        </Title>
        {!movement && (
          <TextInput
            label="Key"
            description="Stable identifier used in the address, e.g. front-plank"
            {...form.getInputProps('key')}
          />
        )}
        <TextInput label="Name" {...form.getInputProps('name')} />
        <Textarea
          label="Description"
          autosize
          minRows={3}
          {...form.getInputProps('description')}
        />
        <ItemList form={form} field="cues" title="Cues" singular="cue" />
        <ItemList
          form={form}
          field="comments"
          title="Comments"
          singular="comment"
        />
        <TagsInput
          label="Tags"
          description="Press Enter to add a tag"
          // A tag is only created with Enter: not by a comma, a paste, or leaving the field.
          splitChars={[]}
          acceptValueOnBlur={false}
          searchValue={pendingTag}
          onSearchChange={setPendingTag}
          {...form.getInputProps('tags')}
        />

        <Stack gap="xs">
          <Text fw={500} size="sm">
            Links to other movements
          </Text>
          {form.values.links.map((_, index) => (
            <Group key={index} align="flex-end" wrap="nowrap">
              <TextInput
                aria-label="Target movement key"
                placeholder="movement-key"
                style={{ flex: 1 }}
                {...form.getInputProps(`links.${index}.target`)}
              />
              <TextInput
                aria-label="Relationship"
                placeholder="progression"
                style={{ flex: 1 }}
                {...form.getInputProps(`links.${index}.relationType`)}
              />
              <ActionIcon
                variant="subtle"
                color="red"
                aria-label="Remove link"
                onClick={() => form.removeListItem('links', index)}
              >
                <IconTrash size={16} />
              </ActionIcon>
            </Group>
          ))}
          <Button
            variant="default"
            size="xs"
            w="fit-content"
            onClick={() =>
              form.insertListItem('links', { target: '', relationType: '' })
            }
          >
            Add link
          </Button>
        </Stack>

        {error && <Alert color="red">{error.message}</Alert>}
        <Group>
          <Button type="submit" loading={busy}>
            Save
          </Button>
          <Button variant="default" onClick={() => navigate(-1)}>
            Cancel
          </Button>
        </Group>
      </Stack>
    </form>
  );
}

/** An ordered list of texts of one item type, with add, remove and reorder. */
function ItemList({
  form,
  field,
  title,
  singular,
}: {
  form: UseFormReturnType<FormValues>;
  field: 'cues' | 'comments';
  title: string;
  singular: string;
}) {
  const values = form.values[field];
  return (
    <Stack gap="xs">
      <Text fw={500} size="sm">
        {title}
      </Text>
      {values.map((_, index) => (
        <Group key={index} align="flex-start" wrap="nowrap">
          <Text size="sm" pt={6} w={20}>
            {index + 1}.
          </Text>
          <Textarea
            aria-label={`${title}, item ${index + 1}`}
            autosize
            minRows={1}
            style={{ flex: 1 }}
            {...form.getInputProps(`${field}.${index}`)}
          />
          <ActionIcon
            variant="default"
            aria-label={`Move ${singular} up`}
            disabled={index === 0}
            onClick={() =>
              form.reorderListItem(field, { from: index, to: index - 1 })
            }
          >
            <IconArrowUp size={16} />
          </ActionIcon>
          <ActionIcon
            variant="default"
            aria-label={`Move ${singular} down`}
            disabled={index === values.length - 1}
            onClick={() =>
              form.reorderListItem(field, { from: index, to: index + 1 })
            }
          >
            <IconArrowDown size={16} />
          </ActionIcon>
          <ActionIcon
            variant="subtle"
            color="red"
            aria-label={`Remove ${singular}`}
            onClick={() => form.removeListItem(field, index)}
          >
            <IconTrash size={16} />
          </ActionIcon>
        </Group>
      ))}
      <Button
        variant="default"
        size="xs"
        w="fit-content"
        onClick={() => form.insertListItem(field, '')}
      >
        Add {singular}
      </Button>
    </Stack>
  );
}
