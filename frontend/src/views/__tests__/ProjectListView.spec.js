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

  it('reveals another page of projects when load more is pressed', async () => {
    const wrapper = mountProjectList()

    expect(wrapper.text()).not.toContain('StudyMate Planner')
    await wrapper.get('button[aria-label="프로젝트 더 불러오기"]').trigger('click')

    expect(wrapper.text()).toContain('StudyMate Planner')
    expect(wrapper.find('[data-testid="pagination"]').exists()).toBe(true)
  })
})
