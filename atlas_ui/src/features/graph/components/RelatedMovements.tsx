import { Alert, Loader, SimpleGrid, Title } from '@mantine/core';
import { MovementCard, MovementCardByKey } from '@/shared/ui';
import type { OutgoingRelatedEdge } from '../api';
import { useEdgeTypes, useEdges } from '../queries';

const capitalize = (text: string) =>
  text.charAt(0).toUpperCase() + text.slice(1);

/**
 * The movements this one is related to, one section per relationship type: if X is related to Y by
 * a progression, X shows a Progressions section with Y's card.
 */
export function RelatedMovements({ movementKey }: { movementKey: string }) {
  const edges = useEdges(movementKey);
  const types = useEdgeTypes();
  // Wait for both, so the sections appear once, with their final headings and order.
  if (edges.isPending || types.isPending) {
    return <Loader size="sm" aria-label="Loading related movements" />;
  }
  if (edges.isError) {
    return (
      <Alert color="red" title="Related movements could not be loaded">
        {edges.error.message}
      </Alert>
    );
  }

  // The types only improve the headings and order, so without them the sections still show.
  const known = types.data ?? [];
  const groups = [
    ...edges.data.outgoing
      .reduce((map, edge) => {
        map.set(edge.type, [...(map.get(edge.type) ?? []), edge]);
        return map;
      }, new Map<string, OutgoingRelatedEdge[]>())
      .entries(),
  ]
    // Known types first, in their usual order; other types after, in the order found.
    .map(([type, group]) => ({
      type,
      group,
      order: known.findIndex((t) => t.name === type),
    }))
    .sort(
      (a, b) => (a.order < 0 ? 99 : a.order) - (b.order < 0 ? 99 : b.order),
    );

  return (
    <>
      {groups.map(({ type, group }) => (
        <section key={type}>
          <Title order={2} size="h3" mb="xs">
            {known.find((t) => t.name === type)?.plural ?? capitalize(type)}
          </Title>
          <SimpleGrid cols={{ base: 1, sm: 2, md: 3 }}>
            {group.map((edge) =>
              edge.movement ? (
                <MovementCard key={edge.target} movement={edge.movement} />
              ) : (
                <MovementCardByKey
                  key={edge.target}
                  movementKey={edge.target}
                />
              ),
            )}
          </SimpleGrid>
        </section>
      ))}
    </>
  );
}
