import { describe, expect, it } from 'vitest';
import { features } from './landingContent';

describe('landing content', () => {
  it('gives every feature card a title and text', () => {
    expect(features.length).toBeGreaterThan(0);
    for (const feature of features) {
      expect(feature.title).not.toBe('');
      expect(feature.text).not.toBe('');
    }
  });
});
