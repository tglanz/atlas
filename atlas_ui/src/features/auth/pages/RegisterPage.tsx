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
import { Link, useNavigate } from 'react-router';
import { useLogin, useRegister } from '../queries';

export function RegisterPage() {
  const register = useRegister();
  const login = useLogin();
  const navigate = useNavigate();
  const form = useForm({
    initialValues: { email: '', password: '' },
    validate: {
      email: (v) =>
        /^\S+@\S+\.\S+$/.test(v.trim()) ? null : 'Enter a valid email',
      password: (v) =>
        v.length >= 8 && v.length <= 72 ? null : 'Use 8 to 72 characters',
    },
  });

  const error = register.error ?? login.error;

  return (
    <Stack maw={400} mx="auto" gap="md">
      <Title order={1}>Register</Title>
      <form
        onSubmit={form.onSubmit((values) =>
          register.mutate(values, {
            // Sign the new user in straight away.
            onSuccess: () =>
              login.mutate(values, { onSuccess: () => navigate('/') }),
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
            description="8 to 72 characters"
            autoComplete="new-password"
            {...form.getInputProps('password')}
          />
          {error && <Alert color="red">{error.message}</Alert>}
          <Button type="submit" loading={register.isPending || login.isPending}>
            Create account
          </Button>
        </Stack>
      </form>
      <Text size="sm">
        Already registered?{' '}
        <Anchor component={Link} to="/login">
          Sign in
        </Anchor>
      </Text>
    </Stack>
  );
}
