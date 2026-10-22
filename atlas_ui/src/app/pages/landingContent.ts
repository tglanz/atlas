import {
  IconBooks,
  IconListCheck,
  IconPhoto,
  IconTopologyStar3,
  type Icon,
} from '@tabler/icons-react';

export const tagline =
  'A visual, interconnected library of human movement designed to make movement knowledge easier to explore, understand, and apply.';

export const features: { title: string; text: string; icon: Icon }[] = [
  {
    title: 'Movement Library',
    icon: IconBooks,
    text: 'A structured catalog of movements, each with clear descriptions, practical cues, and visual illustrations, organized with searchable tags.',
  },
  {
    title: 'Visual Representation',
    icon: IconPhoto,
    text: 'Every movement is illustrated step by step in one consistent visual style, so positions and techniques are easy to understand and compare.',
  },
  {
    title: 'Connected Movements',
    icon: IconTopologyStar3,
    text: 'Movements are linked into a graph of progressions, variants and other relationships, so you can see what leads to a movement and where it can take you next.',
  },
  {
    title: 'Routine Building',
    icon: IconListCheck,
    text: 'Organize movements into personalized routines with ordered sections.',
  },
];
