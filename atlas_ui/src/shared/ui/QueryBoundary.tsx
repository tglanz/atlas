import { Alert, Loader } from '@mantine/core';
import type { UseQueryResult } from '@tanstack/react-query';
import type { ReactNode } from 'react';

/** Shows loading and error states for a query, and renders children once data is there. */
export function QueryBoundary<T>({
  query,
  children,
}: {
  query: UseQueryResult<T>;
  children: (data: T) => ReactNode;
}) {
  if (query.isPending) return <Loader aria-label="Loading" />;
  if (query.isError) {
    return (
      <Alert color="red" title="Something went wrong">
        {query.error.message}
      </Alert>
    );
  }
  return <>{children(query.data)}</>;
}
