import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import InsightsView from '@/views/InsightsView.vue'

function mountInsights() {
  return mount(InsightsView, {
    global: {
      stubs: {
        RouterLink: {
          props: ['to'],
          template: '<a :href="typeof to === \'string\' ? to : to.path"><slot /></a>',
        },
      },
    },
  })
}

describe('InsightsView', () => {
  it('shows the Figma weekly trend ranking in its numbered order', () => {
    const wrapper = mountInsights()

    expect(wrapper.get('h1').text()).toContain('AI 개발 트렌드')
    expect(wrapper.findAll('[data-testid="trend-ranking-card"]')).toHaveLength(4)
    expect(wrapper.findAll('[data-testid="trend-ranking-card"]')[0].text()).toContain(
      'Local LLMs On-Device AI',
    )
    expect(wrapper.findAll('[data-testid="trend-ranking-card"]')[3].text()).toContain(
      'Multimodal RAG Pipeline',
    )
  })

  it('marks the active Figma trend filter and switches its visible ranking', async () => {
    const wrapper = mountInsights()

    await wrapper.get('button[aria-label="AI 에이전트 트렌드"]').trigger('click')

    expect(wrapper.get('button[aria-label="AI 에이전트 트렌드"]').attributes('aria-pressed')).toBe(
      'true',
    )
    expect(wrapper.findAll('[data-testid="trend-ranking-card"]')).toHaveLength(2)
    expect(wrapper.text()).toContain('AI Voice Agent')
    expect(wrapper.text()).not.toContain('Multimodal RAG Pipeline')
  })

  it('links ranked trends to their related project destinations', () => {
    const wrapper = mountInsights()

    expect(wrapper.get('[data-testid="trend-ranking-card"] a').attributes('href')).toBe(
      '/projects/prj_701',
    )
    expect(wrapper.get('[data-testid="maker-opportunities"]').text()).toContain(
      '이번 주 주목해야 할 AI 프로덕트 기회',
    )
  })
})
