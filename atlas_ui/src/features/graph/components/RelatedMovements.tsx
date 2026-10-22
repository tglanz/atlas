import { Anchor, Badge, Group, Stack, Title } from '@mantine/core';
import { Link } from 'react-router';
import type { Edge } from '../api';
import { useEdges } from '../queries';

const capitalize = (text: string) =>
  text.charAt(0).toUpperCase() + text.slice(1);

/** The movements this one points to, grouped by relationship, and the ones that point to it. */
export function RelatedMovements({ movementKey }: { movementKey: string }) {
  const edges = useEdges(movementKey);
  if (!edges.data) return null;

  const { outgoing, incoming } = edges.data;
  const groups = [
    ...outgoing
      .reduce((map, edge) => {
        map.set(edge.type, [...(map.get(edge.type) ?? []), edge]);
        return map;
      }, new Map<string, Edge[]>())
      .entries(),
  ];

  return (
    <>
      {groups.map(([type, group]) => (
        <section key={type}>
          <Title order={2} size="h3" mb="xs">
            {capitalize(type)}
          </Title>
          <Group gap="md">
            {group.map((edge) => (
              <Anchor
                key={edge.target}
                component={Link}
                to={`/movements/${edge.target}`}
              >
                {edge.target}
              </Anchor>
            ))}
          </Group>
        </section>
      ))}
      {incoming.length > 0 && (
        <section>
          <Title order={2} size="h3" mb="xs">
            Linked from
          </Title>
          <Stack gap={4}>
            {incoming.map((edge) => (
              <Group key={`${edge.source}-${edge.type}`} gap="xs">
                <Anchor component={Link} to={`/movements/${edge.source}`}>
                  {edge.source}
                </Anchor>
                <Badge variant="light">{edge.type}</Badge>
              </Group>
            ))}
          </Stack>
        </section>
      )}
    </>
  );
}
