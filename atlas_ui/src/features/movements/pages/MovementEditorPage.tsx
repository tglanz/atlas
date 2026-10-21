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
import { IconTrash } from '@tabler/icons-react';
import { useQueryClient } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router';
import { IllustrationPanel } from '@/features/generation';
import { QueryBoundary } from '@/shared/ui';
import type { MovementContent, MovementDraft } from '../api';
import {
  movementKeys,
  useCreateMovement,
  useCreateRevision,
  useDraft,
  usePublishDraft,
  useUpdateDraft,
} from '../queries';

/** /movements/new creates a draft; /movements/:key/edit edits the open draft. */
export function MovementEditorPage() {
  const { key } = useParams();
  return key ? <EditDraft movementKey={key} /> : <MovementForm />;
}

function EditDraft({ movementKey }: { movementKey: string }) {
  const draft = useDraft(movementKey);
  const createRevision = useCreateRevision();
  const queryClient = useQueryClient();

  return (
    <QueryBoundary query={draft}>
      {(data) =>
        data ? (
          <Stack gap="xl">
            <MovementForm draft={data} />
            <IllustrationPanel
              movementKey={data.key}
              illustrations={data.illustrations}
              onChanged={() =>
                queryClient.invalidateQueries({
                  queryKey: movementKeys.draft(movementKey),
                })
              }
            />
          </Stack>
        ) : (
          <Alert color="blue" title="No open draft">
            <Stack align="flex-start" gap="xs">
              <Text>
                This movement has no draft. Create a new revision to edit it.
              </Text>
              <Button
                loading={createRevision.isPending}
                onClick={() => createRevision.mutate(movementKey)}
              >
                Create revision
              </Button>
              {createRevision.isError && (
                <Text c="red">{createRevision.error.message}</Text>
              )}
            </Stack>
          </Alert>
        )
      }
    </QueryBoundary>
  );
}

type FormValues = {
  key: string;
  name: string;
  description: string;
  cues: string;
  tags: string[];
  links: { target: string; relationType: string }[];
};

const KEY_FORMAT = /^[a-z0-9]+(-[a-z0-9]+)*$/;

function toContent(values: FormValues): MovementContent {
  return {
    name: values.name,
    description: values.description,
    cues: values.cues
      .split('\n')
      .map((cue) => cue.trim())
      .filter(Boolean),
    tags: values.tags,
    links: values.links.filter((link) => link.target && link.relationType),
  };
}

function MovementForm({ draft }: { draft?: MovementDraft }) {
  const navigate = useNavigate();
  const create = useCreateMovement();
  const update = useUpdateDraft();
  const publish = usePublishDraft();

  const form = useForm<FormValues>({
    initialValues: {
      key: draft?.key ?? '',
      name: draft?.name ?? '',
      description: draft?.description ?? '',
      cues: draft?.cues.join('\n') ?? '',
      tags: draft?.tags ?? [],
      links: draft?.links ?? [],
    },
    validate: {
      key: (v) =>
        draft || KEY_FORMAT.test(v)
          ? null
          : 'Use lowercase letters, digits and hyphens',
      name: (v) => (v.trim() ? null : 'Enter a name'),
    },
  });

  const busy = create.isPending || update.isPending || publish.isPending;
  const error = create.error ?? update.error ?? publish.error;

  /** Saves the draft; resolves to the movement key. */
  async function save(values: FormValues): Promise<string> {
    if (draft) {
      await update.mutateAsync({ key: draft.key, content: toContent(values) });
      return draft.key;
    }
    await create.mutateAsync({ key: values.key, content: toContent(values) });
    return values.key;
  }

  return (
    <form
      onSubmit={form.onSubmit(async (values) =>
        navigate(`/movements/${await save(values)}/edit`),
      )}
    >
      <Stack gap="md">
        <Title order={1}>
          {draft ? `Edit ${draft.name || draft.key}` : 'New movement'}
        </Title>
        {draft && <Text c="dimmed">Draft of revision {draft.revision}</Text>}
        {!draft && (
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
        <Textarea
          label="Cues"
          description="One per line, in order"
          autosize
          minRows={3}
          {...form.getInputProps('cues')}
        />
        <TagsInput label="Tags" {...form.getInputProps('tags')} />

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
            Save draft
          </Button>
          <Button
            variant="light"
            loading={busy}
            onClick={() =>
              form.onSubmit(async (values) => {
                const key = await save(values);
                await publish.mutateAsync(key);
                navigate(`/movements/${key}`);
              })()
            }
          >
            Publish
          </Button>
          <Button variant="default" onClick={() => navigate(-1)}>
            Cancel
          </Button>
        </Group>
      </Stack>
    </form>
  );
}
