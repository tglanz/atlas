import {
  Button,
  Card,
  Group,
  SimpleGrid,
  Stack,
  Text,
  ThemeIcon,
  Title,
} from '@mantine/core';
import { Link } from 'react-router';
import { features, tagline } from './landingContent';

export function LandingPage() {
  return (
    <Stack gap="xl" py="xl">
      <Stack
        component="section"
        aria-labelledby="hero-title"
        gap="md"
        py="xl"
        maw={720}
      >
        <Title order={1} id="hero-title" fz={{ base: 36, sm: 56 }}>
          Form Atlas
        </Title>
        <Text size="xl" c="dimmed">
          {tagline}
        </Text>
        <Group>
          <Button component={Link} to="/movements" size="lg">
            Explore movements
          </Button>
          <Button component={Link} to="/routines" size="lg" variant="default">
            Build routines
          </Button>
        </Group>
      </Stack>
      <section aria-labelledby="features-title">
        <Title order={2} id="features-title" mb="md">
          What you can do
        </Title>
        <SimpleGrid cols={{ base: 1, sm: 2 }} spacing="md">
          {features.map(({ title, text, icon: FeatureIcon }) => (
            <Card key={title} withBorder padding="lg" component="article">
              <ThemeIcon size="lg" variant="light" mb="sm" aria-hidden>
                <FeatureIcon size={22} />
              </ThemeIcon>
              <Title order={3} fz="lg" mb="xs">
                {title}
              </Title>
              <Text size="sm" c="dimmed">
                {text}
              </Text>
            </Card>
          ))}
        </SimpleGrid>
      </section>
    </Stack>
  );
}
