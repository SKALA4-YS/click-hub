import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import TutorialsView from '@/views/TutorialsView.vue'

const api = vi.hoisted(() => ({
  getTutorials: vi.fn(),
  getTechnologies: vi.fn(),
}))

vi.mock('@/api/tutorials', () => ({ getTutorials: api.getTutorials }))
vi.mock('@/api/catalog', () => ({ getTechnologies: api.getTechnologies }))

const tutorials = [
  {
    id: 'tutorial-1',
    type: 'DEVELOPMENT',
    difficulty: 'BEGINNER',
    title: 'Spring Boot API 시작하기',
    description: '실제 API가 반환한 튜토리얼입니다.',
    estimatedMinutes: 20,
    technologySlugs: ['spring-boot'],
    sourceUrl: 'https://example.com/spring',
  },
  {
    id: 'tutorial-2',
    type: 'VIBE_CODING',
    difficulty: 'INTERMEDIATE',
    title: 'Vue 화면 빠르게 만들기',
    description: 'Vue 기반 실습 자료입니다.',
    estimatedMinutes: 15,
    technologySlugs: ['vue'],
    sourceUrl: 'https://example.com/vue',
  },
]

describe('TutorialsView', () => {
  beforeEach(() => {
    api.getTutorials.mockReset().mockResolvedValue(tutorials)
    api.getTechnologies.mockReset().mockResolvedValue([
      { id: 'tech-1', slug: 'spring-boot', name: 'Spring Boot' },
      { id: 'tech-2', slug: 'vue', name: 'Vue.js' },
    ])
  })

  it('renders tutorials and technologies returned by the backend', async () => {
    const wrapper = mount(TutorialsView)
    await flushPromises()

    expect(wrapper.text()).toContain('Spring Boot API 시작하기')
    expect(wrapper.text()).toContain('Vue 화면 빠르게 만들기')
    expect(wrapper.get('select[aria-label="튜토리얼 기술"]').text()).toContain('Spring Boot')
    expect(api.getTutorials).toHaveBeenCalledWith({
      type: undefined,
      difficulty: undefined,
      tech: undefined,
    })
  })

  it('reloads tutorials with the selected filters', async () => {
    const wrapper = mount(TutorialsView)
    await flushPromises()

    api.getTutorials.mockClear()
    await wrapper.get('select[aria-label="튜토리얼 난이도"]').setValue('BEGINNER')
    await flushPromises()

    expect(api.getTutorials).toHaveBeenLastCalledWith({
      type: undefined,
      difficulty: 'BEGINNER',
      tech: undefined,
    })
  })

  it('shows a retryable API error', async () => {
    api.getTutorials.mockRejectedValue(new Error('튜토리얼 연결 실패'))
    const wrapper = mount(TutorialsView)
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toBe('튜토리얼 연결 실패')
    expect(wrapper.text()).toContain('다시 시도')
  })
})
