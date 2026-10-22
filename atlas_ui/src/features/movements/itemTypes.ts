import type { MovementItem } from './api';

/**
 * The item types the UI offers and how they are shown. The model allows any type; this is where
 * the usage pattern is decided.
 */
export const ITEM_TYPES = [
  { type: 'CUE', title: 'Cues', singular: 'cue', ordered: true },
  { type: 'COMMENT', title: 'Comments', singular: 'comment', ordered: false },
] as const;

export type ItemGroup = {
  type: string;
  title: string;
  ordered: boolean;
  items: MovementItem[];
};

/** Groups items by type, in the order above, followed by any type the UI does not know. */
export function groupItems(items: MovementItem[]): ItemGroup[] {
  const known = ITEM_TYPES.map((t) => ({
    type: t.type as string,
    title: t.title as string,
    ordered: t.ordered as boolean,
    items: items.filter((i) => i.type === t.type),
  }));
  const knownTypes = new Set<string>(ITEM_TYPES.map((t) => t.type));
  const others = [...new Set(items.map((i) => i.type))]
    .filter((type) => !knownTypes.has(type))
    .map((type) => ({
      type,
      title: type.charAt(0) + type.slice(1).toLowerCase(),
      ordered: false,
      items: items.filter((i) => i.type === type),
    }));
  return [...known, ...others].filter((g) => g.items.length > 0);
}
