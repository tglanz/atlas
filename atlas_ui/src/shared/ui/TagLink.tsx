import { Badge } from '@mantine/core';
import { Link } from 'react-router';
import { formatTag } from './formatTag';

/** A tag that links to the movement index filtered by it. */
export function TagLink({ tag }: { tag: string }) {
  return (
    <Badge
      variant="light"
      component={Link}
      to={`/movements?tags=${encodeURIComponent(tag)}`}
      style={{ cursor: 'pointer' }}
    >
      {formatTag(tag)}
    </Badge>
  );
}
