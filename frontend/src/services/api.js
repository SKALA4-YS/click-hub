const DEFAULT_API_BASE_URL = 'http://localhost:8080'

export function normalizeApiBaseUrl(value = DEFAULT_API_BASE_URL) {
  const baseUrl = value.trim() || DEFAULT_API_BASE_URL
  return baseUrl.replace(/\/+$/, '')
}

export function getApiBaseUrl() {
  return normalizeApiBaseUrl(import.meta.env.VITE_API_BASE_URL)
}

export async function pingBackend({
  baseUrl = getApiBaseUrl(),
  fetchImpl = globalThis.fetch,
  signal,
} = {}) {
  const response = await fetchImpl(`${normalizeApiBaseUrl(baseUrl)}/api/v1/ping`, { signal })

  if (!response.ok) {
    throw new Error(`Backend ping failed with HTTP ${response.status}`)
  }

  const payload = await response.json()

  if (payload.status !== 'ok' || typeof payload.service !== 'string') {
    throw new Error('Backend ping returned an invalid response')
  }

  return payload
}

function sleep(delayMs, signal) {
  return new Promise((resolve, reject) => {
    const timeoutId = setTimeout(() => {
      signal?.removeEventListener('abort', handleAbort)
      resolve()
    }, delayMs)

    function handleAbort() {
      clearTimeout(timeoutId)
      reject(new DOMException('The operation was aborted', 'AbortError'))
    }

    signal?.addEventListener('abort', handleAbort, { once: true })
  })
}

export async function waitForBackend({
  attempts = 19,
  retryDelayMs = 5000,
  fetchImpl = globalThis.fetch,
  signal,
} = {}) {
  let lastError

  for (let attempt = 1; attempt <= attempts; attempt += 1) {
    try {
      return await pingBackend({ fetchImpl, signal })
    } catch (error) {
      if (signal?.aborted) {
        throw error
      }

      lastError = error
      if (attempt < attempts) {
        await sleep(retryDelayMs, signal)
      }
    }
  }

  throw lastError
}
