import {
  IconBooks,
  IconGitBranch,
  IconListCheck,
  IconPhoto,
  type Icon,
} from '@tabler/icons-react';

export const tagline =
  'A visual, interconnected library of human movement designed to make movement knowledge easier to explore, understand, and apply.';

export const features: { title: string; text: string; icon: Icon }[] = [
  {
    title: 'Movement Library',
    icon: IconBooks,
    text: 'A structured catalog of movements, each with clear descriptions, practical cues, and visual illustrations, organized with searchable tags and meaningful relationships.',
  },
  {
    title: 'Visual Representation',
    icon: IconPhoto,
    text: 'Configurable illustrations with consistent figures and viewpoints that help you understand positions and techniques.',
  },
  {
    title: 'Movement Evolution',
    icon: IconGitBranch,
    text: 'Movements evolve over time while preserving their history. Each published version remains available.',
  },
  {
    title: 'Routine Building',
    icon: IconListCheck,
    text: 'Organize movements into personalized routines with ordered sections. Routines reference specific published versions, so their contents stay consistent.',
  },
];
