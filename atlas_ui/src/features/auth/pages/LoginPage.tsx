import {
  Alert,
  Anchor,
  Button,
  PasswordInput,
  Stack,
  Text,
  TextInput,
  Title,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { Link, useLocation, useNavigate } from 'react-router';
import { isApiError } from '@/shared/api';
import { useLogin } from '../queries';

export function LoginPage() {
  const login = useLogin();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/';
  const form = useForm({
    initialValues: { email: '', password: '' },
    validate: {
      email: (v) => (v.trim() ? null : 'Enter your email'),
      password: (v) => (v ? null : 'Enter your password'),
    },
  });

  return (
    <Stack maw={400} mx="auto" gap="md">
      <Title order={1}>Sign in</Title>
      <form
        onSubmit={form.onSubmit((values) =>
          login.mutate(values, {
            onSuccess: () => navigate(from, { replace: true }),
          }),
        )}
      >
        <Stack>
          <TextInput
            label="Email"
            type="email"
            autoComplete="email"
            {...form.getInputProps('email')}
          />
          <PasswordInput
            label="Password"
            autoComplete="current-password"
            {...form.getInputProps('password')}
          />
          {login.isError && (
            <Alert color="red">
              {isApiError(login.error, 401)
                ? 'Incorrect email or password.'
                : login.error.message}
            </Alert>
          )}
          <Button type="submit" loading={login.isPending}>
            Sign in
          </Button>
        </Stack>
      </form>
      <Text size="sm">
        No account?{' '}
        <Anchor component={Link} to="/register">
          Register
        </Anchor>
      </Text>
    </Stack>
  );
}
