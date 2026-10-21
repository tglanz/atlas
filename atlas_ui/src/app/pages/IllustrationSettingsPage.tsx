import { Stack, Tabs, Title } from '@mantine/core';
import { AvatarSettings } from '@/features/avatars';
import { ViewpointSettings } from '@/features/generation';

/** Combines two features, so it lives in the app rather than in either of them. */
export function IllustrationSettingsPage() {
  return (
    <Stack gap="md">
      <Title order={1}>Illustration settings</Title>
      <Tabs defaultValue="avatars">
        <Tabs.List>
          <Tabs.Tab value="avatars">Avatars</Tabs.Tab>
          <Tabs.Tab value="viewpoints">Viewpoints</Tabs.Tab>
        </Tabs.List>
        <Tabs.Panel value="avatars" pt="md">
          <AvatarSettings />
        </Tabs.Panel>
        <Tabs.Panel value="viewpoints" pt="md">
          <ViewpointSettings />
        </Tabs.Panel>
      </Tabs>
    </Stack>
  );
}
