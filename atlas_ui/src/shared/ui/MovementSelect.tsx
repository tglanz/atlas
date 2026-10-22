import { Select } from '@mantine/core';
import { useDebouncedValue } from '@mantine/hooks';
import { useState } from 'react';
import { useMovement, useMovements } from '../movements';

const labelOf = (name: string | undefined, key: string) =>
  !name || name === key ? key : `${name} (${key})`;

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
  // The chosen movement's label comes from the movement itself, never from the search results,
  // so it cannot change as results come and go.
  const selected = useMovement(value, value !== '');
  const selectedLabel = value ? labelOf(selected.data?.name, value) : '';

  // After a choice the control puts that label into the search box. That is not something the user
  // typed, so it must not be searched for.
  const typed = search === selectedLabel ? '' : search;
  const [text] = useDebouncedValue(typed, 250);
  const movements = useMovements({ text });

  const options = (movements.data ?? []).map((m) => ({
    value: m.key,
    label: m.key === value ? selectedLabel : labelOf(m.name, m.key),
  }));
  // Keep the chosen movement in the list even when the current search does not include it.
  if (value && !options.some((o) => o.value === value)) {
    options.unshift({ value, label: selectedLabel });
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
