import { beforeEach, describe, expect, it, vi } from 'vitest'

import { ApiError, createApiClient } from '@/api/client'

function jsonResponse(payload, status = 200) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('API client', () => {
  let fetchImpl

  beforeEach(() => {
    fetchImpl = vi.fn()
  })

  it('normalizes the base URL, repeats array query values and unwraps ApiResponse', async () => {
    fetchImpl.mockResolvedValue(
      jsonResponse({
        success: true,
        status: 200,
        message: 'OK',
        data: { items: [], nextCursor: null, hasNext: false },
      }),
    )
    const client = createApiClient({ baseUrl: 'http://api.test///', fetchImpl })

    await expect(
      client.get('/v1/search', {
        query: { q: 'vue', tags: ['spa', 'web'], cursor: null },
      }),
    ).resolves.toEqual({ items: [], nextCursor: null, hasNext: false })

    const [url, options] = fetchImpl.mock.calls[0]
    expect(url.toString()).toBe('http://api.test/v1/search?q=vue&tags=spa&tags=web')
    expect(options.method).toBe('GET')
  })

  it('adds the bearer token and serializes a JSON body', async () => {
    fetchImpl.mockResolvedValue(
      jsonResponse(
        { success: true, status: 201, message: '생성되었습니다.', data: { id: 'p1' } },
        201,
      ),
    )
    const client = createApiClient({
      baseUrl: 'http://api.test',
      fetchImpl,
      tokenProvider: () => 'access-token',
    })

    await client.post('/v1/projects', {
      auth: 'required',
      body: { title: 'Click HUB' },
    })

    const [, options] = fetchImpl.mock.calls[0]
    expect(options.headers.get('Authorization')).toBe('Bearer access-token')
    expect(options.headers.get('Content-Type')).toBe('application/json')
    expect(options.body).toBe('{"title":"Click HUB"}')
  })

  it('does not send a request when required authentication is missing', async () => {
    const client = createApiClient({
      baseUrl: 'http://api.test',
      fetchImpl,
      tokenProvider: () => null,
    })

    await expect(client.get('/v1/users/me', { auth: 'required' })).rejects.toMatchObject({
      name: 'ApiError',
      status: 401,
      message: '로그인이 필요합니다.',
    })
    expect(fetchImpl).not.toHaveBeenCalled()
  })

  it('maps backend errors and notifies authentication expiry', async () => {
    const onUnauthorized = vi.fn()
    fetchImpl.mockResolvedValue(
      jsonResponse(
        { success: false, status: 401, message: '유효하지 않거나 만료된 토큰입니다.' },
        401,
      ),
    )
    const client = createApiClient({
      baseUrl: 'http://api.test',
      fetchImpl,
      tokenProvider: () => 'expired-token',
      onUnauthorized,
    })

    await expect(client.get('/v1/users/me', { auth: 'required' })).rejects.toEqual(
      expect.objectContaining({
        name: 'ApiError',
        status: 401,
        message: '유효하지 않거나 만료된 토큰입니다.',
      }),
    )
    expect(onUnauthorized).toHaveBeenCalledOnce()
  })

  it('rejects malformed successful responses instead of leaking their raw shape', async () => {
    fetchImpl.mockResolvedValue(jsonResponse({ projects: [] }))
    const client = createApiClient({ baseUrl: 'http://api.test', fetchImpl })

    await expect(client.get('/v1/feed')).rejects.toBeInstanceOf(ApiError)
  })
})
