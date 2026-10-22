import {
  Alert,
  Anchor,
  Badge,
  Button,
  Group,
  Image,
  List,
  Stack,
  Text,
  Title,
} from '@mantine/core';
import { Link, useNavigate, useParams } from 'react-router';
import { useCurrentUser } from '@/features/auth';
import { QueryBoundary } from '@/shared/ui';
import type { Movement, MovementLink } from '../api';
import {
  useCreateRevision,
  useDraft,
  useMovement,
  useRevision,
} from '../queries';

/** Current published revision at /movements/:key, or a specific one at .../revisions/:revision. */
export function MovementDetailsPage() {
  const { key = '', revision } = useParams();
  const current = useMovement(key);
  const historical = useRevision(key, Number(revision));
  const isHistorical = revision !== undefined;

  return (
    <QueryBoundary query={isHistorical ? historical : current}>
      {(movement) => (
        <MovementView movement={movement} historical={isHistorical} />
      )}
    </QueryBoundary>
  );
}

function MovementView({
  movement,
  historical,
}: {
  movement: Movement;
  historical: boolean;
}) {
  const user = useCurrentUser();
  const isMaintainer = user.data?.role === 'MAINTAINER';
  const draft = useDraft(movement.key, isMaintainer);
  const createRevision = useCreateRevision();
  const navigate = useNavigate();

  const linksByType = [
    ...movement.links
      .reduce((groups, link) => {
        groups.set(link.relationType, [
          ...(groups.get(link.relationType) ?? []),
          link,
        ]);
        return groups;
      }, new Map<string, MovementLink[]>())
      .entries(),
  ];

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="flex-start">
        <div>
          <Title order={1}>{movement.name}</Title>
          <Text c="dimmed" size="sm">
            Revision {movement.revision}
            {movement.publishedAt &&
              ` · published ${new Date(movement.publishedAt).toLocaleDateString()}`}
          </Text>
        </div>
        <Group gap="xs">
          <Button
            component={Link}
            to={`/movements/${movement.key}/revisions`}
            variant="default"
            size="xs"
          >
            Revision history
          </Button>
          {isMaintainer && !historical && draft.data && (
            <Button
              component={Link}
              to={`/movements/${movement.key}/edit`}
              size="xs"
            >
              Edit draft
            </Button>
          )}
          {isMaintainer && !historical && draft.data === null && (
            <Button
              size="xs"
              loading={createRevision.isPending}
              onClick={() =>
                createRevision.mutate(movement.key, {
                  onSuccess: () => navigate(`/movements/${movement.key}/edit`),
                })
              }
            >
              Create revision
            </Button>
          )}
        </Group>
      </Group>

      {historical && (
        <Alert color="blue">
          This is an earlier revision.{' '}
          <Anchor component={Link} to={`/movements/${movement.key}`}>
            View the current revision
          </Anchor>
        </Alert>
      )}
      {createRevision.isError && (
        <Alert color="red">{createRevision.error.message}</Alert>
      )}

      {movement.illustrations.length > 0 && (
        <Group gap="md" wrap="wrap">
          {movement.illustrations.map((url, index) => (
            <Image
              key={`${url}-${index}`}
              src={url}
              alt={`${movement.name}, step ${index + 1}`}
              h={220}
              w="auto"
              fit="contain"
            />
          ))}
        </Group>
      )}

      {movement.description && (
        <Text style={{ whiteSpace: 'pre-wrap' }}>{movement.description}</Text>
      )}

      {movement.cues.length > 0 && (
        <section>
          <Title order={2} size="h3" mb="xs">
            Cues
          </Title>
          <List type="ordered">
            {movement.cues.map((cue, index) => (
              <List.Item key={index}>{cue}</List.Item>
            ))}
          </List>
        </section>
      )}

      {movement.tags.length > 0 && (
        <Group gap="xs">
          {movement.tags.map((tag) => (
            <Badge
              key={tag}
              variant="light"
              component={Link}
              to={`/movements?tag=${encodeURIComponent(tag)}`}
            >
              {tag}
            </Badge>
          ))}
        </Group>
      )}

      {linksByType.map(([relationType, links]) => (
        <section key={relationType}>
          <Title
            order={2}
            size="h3"
            mb="xs"
            style={{ textTransform: 'capitalize' }}
          >
            {relationType}
          </Title>
          <Group gap="md">
            {links.map((link) => (
              <Anchor
                key={link.target}
                component={Link}
                to={`/movements/${link.target}`}
              >
                {link.target}
              </Anchor>
            ))}
          </Group>
        </section>
      ))}
    </Stack>
  );
}
