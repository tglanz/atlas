import { Alert, Button, Select, Stack, Text, Title } from '@mantine/core';
import { useState } from 'react';
import { useCurrentUser, useSetPreferredAvatar } from '@/features/auth';
import { QueryBoundary } from '@/shared/ui';
import { useAvatars } from '../queries';

const DEFAULT_CHOICE = 'default';

export function ProfileSettingsPage() {
  const user = useCurrentUser();
  const avatars = useAvatars();
  const save = useSetPreferredAvatar();
  const [choice, setChoice] = useState<string | null>(null);

  return (
    <Stack maw={480} gap="md">
      <Title order={1}>Profile settings</Title>
      <Text c="dimmed">{user.data?.email}</Text>
      <QueryBoundary query={avatars}>
        {(items) => {
          const defaultAvatar = items.find((a) => a.isDefault);
          const current =
            choice ?? user.data?.preferredAvatarId ?? DEFAULT_CHOICE;
          return (
            <Stack>
              <Select
                label="Figure style"
                description="Which kind of figure you see in movement illustrations"
                allowDeselect={false}
                value={current}
                onChange={setChoice}
                data={[
                  {
                    value: DEFAULT_CHOICE,
                    label: defaultAvatar
                      ? `Default (${defaultAvatar.name})`
                      : 'Default',
                  },
                  ...items
                    .filter((a) => !a.isDefault)
                    .map((a) => ({ value: a.id, label: a.name })),
                ]}
              />
              {save.isError && <Alert color="red">{save.error.message}</Alert>}
              {save.isSuccess && <Alert color="green">Saved.</Alert>}
              <Button
                w="fit-content"
                loading={save.isPending}
                onClick={() =>
                  save.mutate(current === DEFAULT_CHOICE ? null : current)
                }
              >
                Save
              </Button>
            </Stack>
          );
        }}
      </QueryBoundary>
    </Stack>
  );
}
