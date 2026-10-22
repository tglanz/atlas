import { describe, expect, it } from 'vitest';
import { KEY_FORMAT, keyFromName } from './movementKey';

describe('keyFromName', () => {
  it('lowercases and turns whitespace into underscores', () => {
    expect(keyFromName('Supinated Pullup')).toBe('supinated_pullup');
    expect(keyFromName('  Wide   Grip  Pull Up ')).toBe('wide_grip_pull_up');
  });

  it('drops characters a key cannot contain', () => {
    expect(keyFromName('Push-up (wide)!')).toBe('push-up_wide');
  });

  it('always produces a valid key when the name has letters or digits', () => {
    for (const name of [
      'Supinated Pullup',
      'A - B',
      '  --x--  ',
      '90 degree hold',
    ]) {
      expect(KEY_FORMAT.test(keyFromName(name))).toBe(true);
    }
    expect(keyFromName('!!!')).toBe('');
  });
});
