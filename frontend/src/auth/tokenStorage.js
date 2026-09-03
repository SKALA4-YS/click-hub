const ACCESS_TOKEN_KEY = 'clickhub.accessToken'

function getStorage() {
  return typeof window === 'undefined' ? null : window.sessionStorage
}

export function getAccessToken() {
  return getStorage()?.getItem(ACCESS_TOKEN_KEY) ?? null
}

export function setAccessToken(token) {
  const storage = getStorage()
  if (!storage) return

  if (token) {
    storage.setItem(ACCESS_TOKEN_KEY, token)
  } else {
    storage.removeItem(ACCESS_TOKEN_KEY)
  }
}

export function clearAccessToken() {
  getStorage()?.removeItem(ACCESS_TOKEN_KEY)
}
