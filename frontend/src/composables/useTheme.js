import { ref, watchEffect } from 'vue'

const STORAGE_KEY = 'clickhub-theme'
const VALID_MODES = ['light', 'dark']

// 기획서 10.2는 라이트/다크/시스템 3단계를 명시하지만, 사용자 요청으로 라이트/다크 2단계로 단순화했다.
// TODO: 로그인 사용자는 계정(서버)에 저장, 비로그인은 localStorage 유지 — 기획서 10.2 다크모드 정책
const mode = ref(localStorage.getItem(STORAGE_KEY) ?? 'light')

function applyTheme(currentMode) {
  document.documentElement.classList.toggle('dark', currentMode === 'dark')
}

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
