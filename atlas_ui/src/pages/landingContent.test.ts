import { describe, expect, it } from 'vitest';
import { features } from './landingContent';

describe('landing content', () => {
  it('has the four documented feature cards', () => {
    expect(features.map((f) => f.title)).toEqual([
      'Movement Library',
      'Visual Representation',
      'Movement Evolution',
      'Routine Building',
    ]);
  });
});
