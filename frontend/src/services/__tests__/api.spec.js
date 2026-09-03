import { describe, expect, it, vi } from 'vitest'

import { normalizeApiBaseUrl, pingBackend, waitForBackend } from '../api'

const successResponse = {
  ok: true,
  json: vi.fn().mockResolvedValue({ status: 'ok', service: 'click-hub-backend' }),
}

describe('backend API client', () => {
  it('normalizes empty values and trailing slashes', () => {
    expect(normalizeApiBaseUrl('')).toBe('http://localhost:8080')
    expect(normalizeApiBaseUrl('https://api.example.com///')).toBe('https://api.example.com')
  })

  it('returns the ping payload', async () => {
    const fetchImpl = vi.fn().mockResolvedValue(successResponse)

    await expect(pingBackend({ baseUrl: 'https://api.example.com/', fetchImpl })).resolves.toEqual({
      status: 'ok',
      service: 'click-hub-backend',
    })
    expect(fetchImpl).toHaveBeenCalledWith('https://api.example.com/api/v1/ping', {
      signal: undefined,
    })
  })

  it('rejects an unsuccessful HTTP response', async () => {
    const fetchImpl = vi.fn().mockResolvedValue({ ok: false, status: 503 })

    await expect(pingBackend({ fetchImpl })).rejects.toThrow('HTTP 503')
  })

  it('retries until the backend responds', async () => {
    const fetchImpl = vi
      .fn()
      .mockRejectedValueOnce(new Error('sleeping'))
      .mockRejectedValueOnce(new Error('starting'))
      .mockResolvedValue(successResponse)

    await expect(waitForBackend({ attempts: 3, retryDelayMs: 0, fetchImpl })).resolves.toEqual({
      status: 'ok',
      service: 'click-hub-backend',
    })
    expect(fetchImpl).toHaveBeenCalledTimes(3)
  })
})
