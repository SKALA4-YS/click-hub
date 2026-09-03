const ACCESS_TOKEN_KEY = 'clickhub.accessToken'
const OAUTH_RETURN_PATH_KEY = 'clickhub.oauthReturnPath'

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

export function saveOAuthReturnPath(path) {
  if (typeof path !== 'string' || !path.startsWith('/') || path.startsWith('//')) return
  getStorage()?.setItem(OAUTH_RETURN_PATH_KEY, path)
}

export function consumeOAuthReturnPath(fallback = '/') {
  const storage = getStorage()
  const path = storage?.getItem(OAUTH_RETURN_PATH_KEY)
  storage?.removeItem(OAUTH_RETURN_PATH_KEY)
  return path && path.startsWith('/') && !path.startsWith('//') ? path : fallback
}
