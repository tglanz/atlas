/**
 * A suggested key for a movement name: lowercase, with whitespace as underscores. Characters a key
 * cannot contain are dropped, so the suggestion is valid whenever the name has letters or digits.
 */
export function keyFromName(name: string): string {
  return name
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '_')
    .replace(/[^a-z0-9_-]/g, '')
    .replace(/[_-]{2,}/g, '_')
    .replace(/^[_-]+|[_-]+$/g, '');
}

/** Lowercase letters and digits, joined by single hyphens or underscores. */
export const KEY_FORMAT = /^[a-z0-9]+([_-][a-z0-9]+)*$/;
