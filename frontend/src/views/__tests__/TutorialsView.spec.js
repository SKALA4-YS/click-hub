import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import TutorialsView from '@/views/TutorialsView.vue'

function mountTutorials() {
  return mount(TutorialsView, {
    attachTo: document.body,
    global: {
      stubs: {
        RouterLink: {
          props: ['to'],
          template: '<a :href="to"><slot /></a>',
        },
      },
    },
  })
}

function getFilter(wrapper, label) {
  return wrapper
    .findAll('button')
    .find((button) => button.attributes('aria-label') === `${label} 필터`)
}

let writeText

beforeEach(() => {
  document.body.innerHTML = ''
  writeText = vi.fn().mockResolvedValue(undefined)
  Object.defineProperty(navigator, 'clipboard', {
    configurable: true,
    value: { writeText },
  })
})

describe('TutorialsView', () => {
  it('renders the Figma roadmap hero and all five practical curriculum cards', () => {
    const wrapper = mountTutorials()

    expect(wrapper.get('h1').text()).toContain('아이디어만으로 웹사이트 런칭까지')
    expect(wrapper.findAll('[data-testid="roadmap-card"]')).toHaveLength(5)
    expect(wrapper.text()).toContain('Step 3. Cursor IDE로 에러 없이 조립하는 실전 테크닉')
    expect(wrapper.get('[data-testid="tutorial-progress"]').text()).toContain('40%')
    expect(wrapper.get('[role="progressbar"]').attributes()).toMatchObject({
      'aria-valuemin': '0',
      'aria-valuemax': '5',
      'aria-valuenow': '2',
    })
    expect(wrapper.find('main').exists()).toBe(false)
  })

  it.each([
    ['기획 & 프롬프팅', 'Step 1. 막연한 아이디어를 AI가 이해하는 기획서(PRD)로 바꾸기'],
    ['AI 도구', 'Step 3. Cursor IDE로 에러 없이 조립하는 실전 테크닉'],
    ['UI/UX & 프론트엔드', 'Step 2. 디자인 툴 없이 자연어로 고화질 반응형 UI 뽑아내기'],
    ['DB & 백엔드', 'Step 4. 복잡한 백엔드 없이 10분 만에 Supabase DB & 로그인 붙이기'],
    ['원클릭 런칭 & 배포', 'Step 5. Vercel 원클릭 무료 배포와 커스텀 도메인 연결'],
  ])('shows only the matching roadmap card for %s', async (label, title) => {
    const wrapper = mountTutorials()

    await getFilter(wrapper, label).trigger('click')

    expect(wrapper.findAll('[data-testid="roadmap-card"]')).toHaveLength(1)
    expect(wrapper.text()).toContain(title)
    expect(getFilter(wrapper, label).attributes('aria-pressed')).toBe('true')
    expect(getFilter(wrapper, '전체 로드맵').attributes('aria-pressed')).toBe('false')
  })

  it('restores every roadmap card when the all-roadmap filter is selected again', async () => {
    const wrapper = mountTutorials()
    const backendFilter = getFilter(wrapper, 'DB & 백엔드')

    await backendFilter.trigger('click')
    await wrapper.get('button[aria-label="전체 로드맵 필터"]').trigger('click')

    expect(wrapper.findAll('[data-testid="roadmap-card"]')).toHaveLength(5)
    expect(getFilter(wrapper, '전체 로드맵').attributes('aria-pressed')).toBe('true')
  })

  it('focuses the first step when the hero start action is used', async () => {
    const wrapper = mountTutorials()

    await wrapper.get('button[aria-label="1강부터 바로 시작하기"]').trigger('click')

    expect(document.activeElement).toBe(wrapper.get('#roadmap-step-planning').element)
  })

  it('focuses the current step when the progress action is used', async () => {
    const wrapper = mountTutorials()

    await wrapper.get('button[aria-label="Step 3 이어서 학습하기"]').trigger('click')

    expect(document.activeElement).toBe(wrapper.get('#roadmap-step-ai').element)
  })

  it.each([
    ['첫 번째 프롬프트 템플릿 복사', '실리콘밸리 최고의 스타트업'],
    ['1번 프롬프트 복사', '실리콘밸리 최고의 스타트업'],
    ['2번 프롬프트 복사', 'Cursor Composer 모드'],
    ['3번 프롬프트 복사', 'Supabase Auth 구글 OAuth'],
  ])('copies %s to the local clipboard and announces the result', async (label, copiedText) => {
    const wrapper = mountTutorials()

    await wrapper.get(`button[aria-label="${label}"]`).trigger('click')

    expect(writeText).toHaveBeenCalledWith(expect.stringContaining(copiedText))
    expect(wrapper.get('[role="status"]').text()).toBe('프롬프트를 복사했습니다.')
  })

  it('reports when the local Clipboard API is unavailable', async () => {
    Object.defineProperty(navigator, 'clipboard', { configurable: true, value: undefined })
    const wrapper = mountTutorials()

    await wrapper.get('button[aria-label="1번 프롬프트 복사"]').trigger('click')

    expect(wrapper.get('[role="status"]').text()).toBe('클립보드를 사용할 수 없습니다.')
  })
})
