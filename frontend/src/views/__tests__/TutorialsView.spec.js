import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import TutorialsView from '@/views/TutorialsView.vue'

function mountTutorials() {
  return mount(TutorialsView, {
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

describe('TutorialsView', () => {
  it('renders the Figma roadmap hero and all five practical curriculum cards', () => {
    const wrapper = mountTutorials()

    expect(wrapper.get('h1').text()).toContain('아이디어만으로 웹사이트 런칭까지')
    expect(wrapper.findAll('[data-testid="roadmap-card"]')).toHaveLength(5)
    expect(wrapper.text()).toContain('Step 3. Cursor IDE로 에러 없이 조립하는 실전 테크닉')
    expect(wrapper.get('[data-testid="tutorial-progress"]').text()).toContain('40%')
  })

  it('shows the matching roadmap card when a Figma course filter is selected', async () => {
    const wrapper = mountTutorials()

    await wrapper.get('button[aria-label="AI 도구 필터"]').trigger('click')

    expect(wrapper.findAll('[data-testid="roadmap-card"]')).toHaveLength(1)
    expect(wrapper.text()).toContain('Cursor IDE로 에러 없이 조립하는 실전 테크닉')
    expect(wrapper.text()).not.toContain('복잡한 백엔드 없이 10분 만에 Supabase DB')
  })

  it('restores every roadmap card when the all-roadmap filter is selected again', async () => {
    const wrapper = mountTutorials()
    const backendFilter = wrapper
      .findAll('button')
      .find((button) => button.attributes('aria-label') === 'DB & 백엔드 필터')

    await backendFilter.trigger('click')
    await wrapper.get('button[aria-label="전체 로드맵 필터"]').trigger('click')

    expect(wrapper.findAll('[data-testid="roadmap-card"]')).toHaveLength(5)
  })
})
