import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiFetch } from './client';
import { ApiError } from './errors';

afterEach(() => vi.unstubAllGlobals());

function stubFetch(response: Response) {
  const fetchMock = vi.fn().mockResolvedValue(response);
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

describe('apiFetch', () => {
  it('prefixes /api, sends cookies and parses JSON', async () => {
    const fetchMock = stubFetch(Response.json([{ key: 'plank' }]));
    const result = await apiFetch<{ key: string }[]>('/movements');
    expect(result).toEqual([{ key: 'plank' }]);
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/movements',
      expect.objectContaining({ method: 'GET', credentials: 'include' }),
    );
  });

  it('throws ApiError with the problem detail', async () => {
    stubFetch(
      Response.json({ detail: 'No published movement x' }, { status: 404 }),
    );
    const error = await apiFetch('/movements/x').catch((e: unknown) => e);
    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({
      status: 404,
      message: 'No published movement x',
    });
  });

  it('returns undefined for 204', async () => {
    stubFetch(new Response(null, { status: 204 }));
    expect(await apiFetch('/auth/logout')).toBeUndefined();
  });
});
