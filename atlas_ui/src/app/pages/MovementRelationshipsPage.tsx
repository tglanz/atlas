import {
  ActionIcon,
  Alert,
  Anchor,
  Button,
  Group,
  Select,
  Stack,
  Text,
  Title,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconTrash } from '@tabler/icons-react';
import { Link, useNavigate, useParams } from 'react-router';
import { useEdgeTypes, useEdges, useSetOutgoing } from '@/features/graph';
import type { Edge, EdgeType } from '@/features/graph';
import { MovementSelect, useMovement } from '@/features/movements';
import { QueryBoundary } from '@/shared/ui';

type Row = { target: string; type: string };

/** Combines the graph and the movements, so it lives in the app rather than in either. */
export function MovementRelationshipsPage() {
  const { key = '' } = useParams();
  const movement = useMovement(key);
  const edges = useEdges(key);
  const types = useEdgeTypes();

  return (
    <QueryBoundary query={movement}>
      {(m) => (
        <QueryBoundary query={edges}>
          {(e) => (
            <QueryBoundary query={types}>
              {(t) => (
                <RelationshipsForm
                  movementKey={m.key}
                  movementName={m.name}
                  outgoing={e.outgoing}
                  incoming={e.incoming}
                  types={t}
                />
              )}
            </QueryBoundary>
          )}
        </QueryBoundary>
      )}
    </QueryBoundary>
  );
}

function RelationshipsForm({
  movementKey,
  movementName,
  outgoing,
  incoming,
  types,
}: {
  movementKey: string;
  movementName: string;
  outgoing: Edge[];
  incoming: Edge[];
  types: EdgeType[];
}) {
  const navigate = useNavigate();
  const save = useSetOutgoing(movementKey);
  const form = useForm<{ links: Row[] }>({
    initialValues: {
      links: outgoing.map((e) => ({ target: e.target, type: e.type })),
    },
    validate: {
      links: {
        target: (v) => (v ? null : 'Choose a movement'),
        type: (v) => (v ? null : 'Choose a type'),
      },
    },
  });

  // Offer the known types, plus any other type an existing relationship already uses.
  const known = types.map((t) => ({ value: t.name, label: t.label }));
  const extra = [...new Set(form.values.links.map((l) => l.type))]
    .filter((type) => type && !types.some((t) => t.name === type))
    .map((type) => ({ value: type, label: type }));
  const typeOptions = [...known, ...extra];

  const labelOf = (type: string) =>
    typeOptions.find((o) => o.value === type)?.label ?? type;

  return (
    <form
      onSubmit={form.onSubmit((values) =>
        save.mutate(values.links, {
          onSuccess: () => navigate(`/movements/${movementKey}`),
        }),
      )}
    >
      <Stack gap="md">
        <Title order={1}>Relationships of {movementName}</Title>
        <Anchor component={Link} to={`/movements/${movementKey}`}>
          Back to {movementName}
        </Anchor>

        <Stack gap="xs">
          <Text fw={500} size="sm">
            From this movement to
          </Text>
          {form.values.links.length === 0 && (
            <Text c="dimmed" size="sm">
              No relationships yet.
            </Text>
          )}
          {form.values.links.map((_, index) => (
            <Group key={index} align="flex-start" wrap="nowrap">
              <div style={{ flex: 2 }}>
                <MovementSelect
                  ariaLabel={`Relationship ${index + 1}, movement`}
                  value={form.values.links[index].target}
                  onChange={(target) =>
                    form.setFieldValue(`links.${index}.target`, target)
                  }
                />
                {form.errors[`links.${index}.target`] && (
                  <Text c="red" size="xs">
                    {form.errors[`links.${index}.target`]}
                  </Text>
                )}
              </div>
              <Select
                style={{ flex: 1 }}
                aria-label={`Relationship ${index + 1}, type`}
                placeholder="Type"
                data={typeOptions}
                {...form.getInputProps(`links.${index}.type`)}
              />
              <ActionIcon
                variant="subtle"
                color="red"
                aria-label={`Remove relationship ${index + 1}`}
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
              form.insertListItem('links', { target: '', type: '' })
            }
          >
            Add relationship
          </Button>
          {types.some((t) => t.bidirectional) && (
            <Text size="xs" c="dimmed">
              {types
                .filter((t) => t.bidirectional)
                .map((t) => t.label)
                .join(', ')}{' '}
              relationships also appear on the other movement.
            </Text>
          )}
        </Stack>

        {incoming.length > 0 && (
          <Stack gap={4}>
            <Text fw={500} size="sm">
              From other movements to this one
            </Text>
            {incoming.map((edge) => (
              <Text key={`${edge.source}-${edge.type}`} size="sm">
                <Anchor component={Link} to={`/movements/${edge.source}`}>
                  {edge.source}
                </Anchor>{' '}
                ({labelOf(edge.type)})
              </Text>
            ))}
          </Stack>
        )}

        {save.isError && <Alert color="red">{save.error.message}</Alert>}
        <Group>
          <Button type="submit" loading={save.isPending}>
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
