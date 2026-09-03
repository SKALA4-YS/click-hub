import { ref, watchEffect } from 'vue'

const STORAGE_KEY = 'clickhub-theme'
const VALID_MODES = ['light', 'dark', 'system']

// TODO: 로그인 사용자는 계정(서버)에 저장, 비로그인은 localStorage 유지 — 기획서 10.2 다크모드 정책
const mode = ref(localStorage.getItem(STORAGE_KEY) ?? 'system')

function resolveIsDark(currentMode) {
  if (currentMode === 'system') {
    return window.matchMedia('(prefers-color-scheme: dark)').matches
  }
  return currentMode === 'dark'
}

function applyTheme(currentMode) {
  document.documentElement.classList.toggle('dark', resolveIsDark(currentMode))
}

const media = window.matchMedia('(prefers-color-scheme: dark)')
media.addEventListener('change', () => {
  if (mode.value === 'system') applyTheme('system')
})

watchEffect(() => {
  localStorage.setItem(STORAGE_KEY, mode.value)
  applyTheme(mode.value)
})

export function useTheme() {
  function setMode(nextMode) {
    if (!VALID_MODES.includes(nextMode)) return
    mode.value = nextMode
  }

  function cycleMode() {
    const currentIndex = VALID_MODES.indexOf(mode.value)
    setMode(VALID_MODES[(currentIndex + 1) % VALID_MODES.length])
  }

  return { mode, setMode, cycleMode }
}
