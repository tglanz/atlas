import { Select } from '@mantine/core';
import { useDebouncedValue } from '@mantine/hooks';
import { useState } from 'react';
import { useMovements } from '../queries';

/** A form control for choosing one movement by searching for it. The value is the movement key. */
export function MovementSelect({
  value,
  onChange,
  label,
  ariaLabel,
  placeholder = 'Search movements',
}: {
  value: string;
  onChange: (movementKey: string) => void;
  label?: string;
  ariaLabel?: string;
  placeholder?: string;
}) {
  const [search, setSearch] = useState('');
  const [text] = useDebouncedValue(search, 250);
  const movements = useMovements({ text });

  const options = (movements.data ?? []).map((m) => ({
    value: m.key,
    label: m.name === m.key ? m.key : `${m.name} (${m.key})`,
  }));
  // Keep the chosen movement selectable even when the current search does not list it.
  if (value && !options.some((o) => o.value === value)) {
    options.unshift({ value, label: value });
  }

  return (
    <Select
      label={label}
      aria-label={ariaLabel}
      placeholder={placeholder}
      searchable
      data={options}
      value={value || null}
      onChange={(next) => onChange(next ?? '')}
      searchValue={search}
      onSearchChange={setSearch}
      // The list is already filtered by the server.
      filter={({ options: all }) => all}
      nothingFoundMessage={
        movements.isPending ? 'Searching…' : 'No movements found'
      }
    />
  );
}
