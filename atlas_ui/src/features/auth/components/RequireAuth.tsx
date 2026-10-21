import { Alert, Loader } from '@mantine/core';
import { Navigate, Outlet, useLocation } from 'react-router';
import type { Role } from '../api';
import { useCurrentUser } from '../queries';

/**
 * Route guard. Visitors go to the login page; users without the required role see a notice.
 * This only shapes the UI; the backend enforces access.
 */
export function RequireAuth({ role }: { role?: Role }) {
  const user = useCurrentUser();
  const location = useLocation();

  if (user.isPending) return <Loader aria-label="Loading" />;
  if (user.isError) {
    return (
      <Alert color="red" title="Could not check your session">
        {user.error.message}
      </Alert>
    );
  }
  if (!user.data) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location.pathname + location.search }}
      />
    );
  }
  if (role && user.data.role !== role) {
    return (
      <Alert color="yellow" title="Not allowed">
        This page is for maintainers.
      </Alert>
    );
  }
  return <Outlet />;
}
