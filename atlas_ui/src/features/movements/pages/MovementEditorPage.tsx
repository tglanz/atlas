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
import { IllustrationGeneration } from '@/features/generation';
import { useEdges, useSetOutgoing } from '@/features/graph';
import type { Edge } from '@/features/graph';
import { QueryBoundary } from '@/shared/ui';
import type {
  Movement,
  MovementContent,
  MovementItem,
  MovementStage,
} from '@/shared/movements';
import { LinkRows } from '../components/LinkRows';
import { StageRows } from '../components/StageRows';
import { KEY_FORMAT, keyFromName } from '../movementKey';
import {
  movementKeys,
  useCreateMovement,
  useMovement,
  useUpdateMovement,
} from '@/shared/movements';

/** /movements/new creates a movement; /movements/:key/edit changes one. */
export function MovementEditorPage() {
  const { key } = useParams();
  return key ? <EditMovement movementKey={key} /> : <MovementForm />;
}

function EditMovement({ movementKey }: { movementKey: string }) {
  const movement = useMovement(movementKey);
  const edges = useEdges(movementKey);

  return (
    <QueryBoundary query={movement}>
      {(data) => (
        <QueryBoundary query={edges}>
          {(related) => (
            <MovementForm movement={data} outgoing={related.outgoing} />
          )}
        </QueryBoundary>
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
  stages: MovementStage[];
  tags: string[];
  // Edges leaving this movement. They are stored in the graph, not in the movement.
  links: { target: string; type: string }[];
};

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
    stages: values.stages.map((s) => ({
      name: s.name.trim(),
      description: s.description.trim(),
    })),
    tags: withPendingTag(values.tags, pendingTag),
  };
}

function contentsOf(movement: Movement | undefined, type: string): string[] {
  return (
    movement?.items.filter((i) => i.type === type).map((i) => i.content) ?? []
  );
}

function MovementForm({
  movement,
  outgoing,
}: {
  movement?: Movement;
  outgoing?: Edge[];
}) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const create = useCreateMovement();
  const update = useUpdateMovement();
  const setOutgoing = useSetOutgoing();
  // Set once a new movement has been created, so a later save updates it instead of creating again.
  const [createdKey, setCreatedKey] = useState<string | null>(null);
  const existingKey = movement?.key ?? createdKey;
  // Whether the maintainer has written the key themselves, which stops it following the name.
  const [keyTouched, setKeyTouched] = useState(false);
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
      stages: movement?.stages ?? [],
      tags: movement?.tags ?? [],
      links: outgoing?.map((e) => ({ target: e.target, type: e.type })) ?? [],
    },
    validate: {
      key: (v) =>
        existingKey || KEY_FORMAT.test(v)
          ? null
          : 'Use lowercase letters, digits, hyphens and underscores',
      name: (v) => (v.trim() ? null : 'Enter a name'),
      stages: (rows) =>
        rows.every((row) => row.name.trim() && row.description.trim())
          ? null
          : 'Give every stage a name and a description',
      links: (rows) =>
        rows.every((row) => row.target && row.type)
          ? null
          : 'Choose a movement and a type for every relationship',
    },
  });

  const busy = create.isPending || update.isPending || setOutgoing.isPending;
  const error = create.error ?? update.error ?? setOutgoing.error;

  /** Saves the details and the relationships. Illustrations are never changed by saving. */
  async function save(values: FormValues) {
    const content = toContent(values, pendingTag);
    let key = existingKey;
    if (key) {
      await update.mutateAsync({ key, content });
    } else {
      key = values.key;
      await create.mutateAsync({ key, content });
      setCreatedKey(key);
    }
    if (existingKey || values.links.length > 0) {
      await setOutgoing.mutateAsync({ movementKey: key, edges: values.links });
    }
    navigate(`/movements/${key}`);
  }

  return (
    <form onSubmit={form.onSubmit(save)}>
      <Stack gap="md">
        <Title order={1}>
          {movement ? `Edit ${movement.name}` : 'New movement'}
        </Title>
        <TextInput
          label="Name"
          {...form.getInputProps('name')}
          onChange={(event) => {
            const name = event.currentTarget.value;
            form.setFieldValue('name', name);
            // Suggest a key from the name until the maintainer writes their own.
            if (!keyTouched) form.setFieldValue('key', keyFromName(name));
          }}
        />
        {!movement && (
          <TextInput
            label="Key"
            description="Identifier used in the address. Suggested from the name; you can change it."
            disabled={createdKey !== null}
            {...form.getInputProps('key')}
            onChange={(event) => {
              const key = event.currentTarget.value;
              form.setFieldValue('key', key);
              // Emptying the field hands control back to the name.
              setKeyTouched(key !== '');
            }}
          />
        )}
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
        <StageRows
          value={form.values.stages}
          onChange={(rows) => form.setFieldValue('stages', rows)}
          error={
            typeof form.errors.stages === 'string'
              ? form.errors.stages
              : undefined
          }
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

        <LinkRows
          value={form.values.links}
          onChange={(rows) => form.setFieldValue('links', rows)}
          error={
            typeof form.errors.links === 'string'
              ? form.errors.links
              : undefined
          }
        />

        <Stack gap="sm">
          <Title order={2} size="h3">
            Illustrations
          </Title>
          {movement ? (
            <IllustrationGeneration
              movementKey={movement.key}
              illustrations={movement.illustrations}
              stages={movement.stages}
              onChanged={() =>
                queryClient.invalidateQueries({ queryKey: movementKeys.all })
              }
            />
          ) : (
            <Text size="sm" c="dimmed">
              Save the movement first, then edit it to generate illustrations.
            </Text>
          )}
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
