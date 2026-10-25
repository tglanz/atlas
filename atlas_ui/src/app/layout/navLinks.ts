export const navLinks = [
  { label: 'Movements', to: '/movements' },
  { label: 'Routines', to: '/routines' },
] as const;

/** Only for maintainers. The backend enforces this too. */
export const maintainerLinks = [
  { label: 'Illustrations', to: '/settings/illustrations' },
] as const;
