import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

vi.mock('vue-router', () => ({
  RouterLink: { props: ['to'], template: '<a :href="to"><slot /></a>' },
  useRoute: () => ({ query: {} }),
}))

import ProjectListView from '@/views/ProjectListView.vue'

function mountProjectList() {
  return mount(ProjectListView, {
    global: {
      stubs: {
        RouterLink: { props: ['to'], template: '<a :href="to"><slot /></a>' },
      },
    },
  })
}

describe('ProjectListView', () => {
  it('shows the Figma top eight projects in ranking order by default', () => {
    const wrapper = mountProjectList()

    expect(wrapper.findAll('.project-grid h3').map((card) => card.text())).toEqual([
      'DevFlow Analytics',
      'PromptCraft Studio',
      'IconGenie Studio',
      'CodeSnap Pro',
      'LogStack Pro',
      'GrowthPulse',
      'GitPulse Activity',
      'FlowBoard Kanban',
    ])
    expect(wrapper.get('[aria-label="프로젝트 수"]').text()).toBe('148개 프로젝트')
    for (const page of [1, 2, 3, 4, 12]) {
      expect(wrapper.get(`button[aria-label="${page}페이지"]`).exists()).toBe(true)
    }
    expect(wrapper.get('[data-testid="pagination"]').text()).toContain('…')
    expect(wrapper.text()).toContain('AI 프롬프트 체이닝과 버전 관리를 위한 협업 워크스페이스')
    expect(wrapper.text()).toContain('4.1k')
  })

  it('filters the visible cards when a category is selected', async () => {
    const wrapper = mountProjectList()

    await wrapper.get('button[aria-label="디자인 카테고리"]').trigger('click')

    expect(wrapper.text()).toContain('IconGenie Studio')
    expect(wrapper.text()).not.toContain('DevFlow Analytics')
    expect(wrapper.get('[aria-label="프로젝트 수"]').text()).toBe('2개 프로젝트')
  })

  it('sorts the project cards by the selected order', async () => {
    const wrapper = mountProjectList()

    await wrapper.get('[aria-label="프로젝트 정렬"]').setValue('latest')

    expect(wrapper.findAll('.project-grid h3').map((card) => card.text())).toEqual([
      'TinyCRM',
      'ClipCast Studio',
      'PixelMuse AI',
      'ReceiptRadar',
      'StudyMate Planner',
      'MoodPalette',
      'CodeSnap Pro',
      'GitPulse Activity',
    ])
  })

  it.each([
    ['AI', 'PromptCraft Studio'],
    ['마케팅', 'GrowthPulse'],
  ])('keeps the Figma %s classification searchable', async (category, title) => {
    const wrapper = mountProjectList()

    await wrapper.get('button[aria-label="' + category + ' 카테고리"]').trigger('click')

    expect(wrapper.text()).toContain(title)
  })

  it('switches between the Figma grid and list presentations', async () => {
    const wrapper = mountProjectList()
    const listButton = wrapper.get('button[aria-label="목록 보기"]')

    await listButton.trigger('click')

    expect(listButton.attributes('aria-pressed')).toBe('true')
    expect(wrapper.get('.project-grid').classes()).toContain('grid-cols-1')
  })

  it('replaces the visible slice when another page is selected', async () => {
    const wrapper = mountProjectList()

    expect(wrapper.text()).not.toContain('StudyMate Planner')
    await wrapper.get('button[aria-label="2페이지"]').trigger('click')

    expect(wrapper.text()).toContain('StudyMate Planner')
    expect(wrapper.text()).not.toContain('DevFlow Analytics')
    expect(wrapper.get('button[aria-label="2페이지"]').attributes('aria-current')).toBe('page')
  })
})
