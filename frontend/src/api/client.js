import { clearAccessToken, getAccessToken } from '@/auth/tokenStorage'

const DEFAULT_API_BASE_URL = 'http://localhost:8080'
const DEFAULT_TIMEOUT_MS = 10_000

export class ApiError extends Error {
  constructor(message, { status = 0, data = null, cause } = {}) {
    super(message, { cause })
    this.name = 'ApiError'
    this.status = status
    this.data = data
  }
}

function normalizeBaseUrl(baseUrl) {
  return (baseUrl || DEFAULT_API_BASE_URL).replace(/\/+$/, '')
}

export function getApiUrl(path, baseUrl = import.meta.env.VITE_API_BASE_URL) {
  if (!path.startsWith('/')) {
    throw new TypeError(`API path must start with "/": ${path}`)
  }
  return `${normalizeBaseUrl(baseUrl)}${path}`
}

function appendQuery(url, query = {}) {
  Object.entries(query).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return

    const values = Array.isArray(value) ? value : [value]
    values.forEach((item) => url.searchParams.append(key, String(item)))
  })
  return url
}

async function parseResponse(response) {
  if (response.status === 204) return null

  const text = await response.text()
  if (!text) return null

  try {
    return JSON.parse(text)
  } catch (error) {
    throw new ApiError('서버 응답을 해석할 수 없습니다.', {
      status: response.status,
      cause: error,
    })
  }
}

function notifyUnauthorized() {
  clearAccessToken()
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('clickhub:unauthorized'))
  }
}

export function createApiClient({
  baseUrl = import.meta.env.VITE_API_BASE_URL,
  fetchImpl = globalThis.fetch,
  tokenProvider = getAccessToken,
  onUnauthorized = notifyUnauthorized,
  timeoutMs = DEFAULT_TIMEOUT_MS,
} = {}) {
  const normalizedBaseUrl = normalizeBaseUrl(baseUrl)

  async function request(
    path,
    { method = 'GET', query, body, signal, auth = 'optional', headers = {} } = {},
  ) {
    if (!path.startsWith('/')) {
      throw new TypeError(`API path must start with "/": ${path}`)
    }

    const token = tokenProvider?.()
    if (auth === 'required' && !token) {
      throw new ApiError('로그인이 필요합니다.', { status: 401 })
    }

    const controller = new AbortController()
    let timedOut = false
    const timeoutId = setTimeout(() => {
      timedOut = true
      controller.abort()
    }, timeoutMs)
    const abortFromCaller = () => controller.abort(signal?.reason)
    signal?.addEventListener('abort', abortFromCaller, { once: true })

    const requestHeaders = new Headers(headers)
    requestHeaders.set('Accept', 'application/json')
    if (body !== undefined) requestHeaders.set('Content-Type', 'application/json')
    if (token && auth !== 'none') requestHeaders.set('Authorization', `Bearer ${token}`)

    try {
      const url = appendQuery(new URL(getApiUrl(path, normalizedBaseUrl)), query)
      const response = await fetchImpl(url, {
        method,
        headers: requestHeaders,
        body: body === undefined ? undefined : JSON.stringify(body),
        signal: controller.signal,
      })
      const payload = await parseResponse(response)

      if (response.status === 401 && auth !== 'none') onUnauthorized?.()

      if (!response.ok || payload?.success === false) {
        throw new ApiError(payload?.message || `요청에 실패했습니다. (${response.status})`, {
          status: response.status,
          data: payload?.data ?? null,
        })
      }

      if (payload === null) return null
      if (typeof payload.success !== 'boolean' || !('data' in payload)) {
        throw new ApiError('서버 응답 형식이 올바르지 않습니다.', {
          status: response.status,
          data: payload,
        })
      }

      return payload.data
    } catch (error) {
      if (error instanceof ApiError) throw error
      if (error?.name === 'AbortError') {
        throw new ApiError(timedOut ? '요청 시간이 초과되었습니다.' : '요청이 취소되었습니다.', {
          cause: error,
        })
      }
      throw new ApiError('서버에 연결할 수 없습니다.', { cause: error })
    } finally {
      clearTimeout(timeoutId)
      signal?.removeEventListener('abort', abortFromCaller)
    }
  }

  return {
    request,
    get: (path, options) => request(path, { ...options, method: 'GET' }),
    post: (path, options) => request(path, { ...options, method: 'POST' }),
    put: (path, options) => request(path, { ...options, method: 'PUT' }),
    patch: (path, options) => request(path, { ...options, method: 'PATCH' }),
    delete: (path, options) => request(path, { ...options, method: 'DELETE' }),
  }
}

export const apiClient = createApiClient()
