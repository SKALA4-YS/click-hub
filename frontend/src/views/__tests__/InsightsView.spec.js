import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { describe, expect, it } from 'vitest'

import router from '@/router'
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
    expect(wrapper.text()).toContain('WEEKLY AI TREND REPORT · 2026년 9월 1주')
    expect(wrapper.findAll('[data-testid="hero-metric"]')).toHaveLength(3)
    expect(wrapper.text()).toContain('이번 주 인사이트 리포트 다운로드')
    expect(wrapper.text()).toContain('트렌드 키워드로 바로 프로젝트 시작하기')
    expect(wrapper.get('[aria-label="이전 주 리포트"]').exists()).toBe(true)
    expect(wrapper.get('[aria-label="다음 주 리포트"]').exists()).toBe(true)
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

    await wrapper.get('button[aria-label="급상승 검색어 트렌드"]').trigger('click')

    expect(wrapper.findAll('[role="group"] button')).toHaveLength(5)
    expect(
      wrapper.get('button[aria-label="급상승 검색어 트렌드"]').attributes('aria-pressed'),
    ).toBe('true')
    expect(wrapper.findAll('[data-testid="trend-ranking-card"]')).toHaveLength(2)
    expect(wrapper.text()).toContain('AI Voice Agent')
    expect(wrapper.text()).not.toContain('Multimodal RAG Pipeline')
  })

  it('shows point rows, the fourth sidebar CTA, and the 2 by 2 technology stack', () => {
    const wrapper = mountInsights()

    expect(wrapper.findAll('[data-testid="trend-point-row"]')).toHaveLength(4)
    expect(wrapper.text()).toContain('B2B 채택 1위')
    expect(wrapper.get('[data-testid="trend-discussion-cta"]').text()).toContain('트렌드 토론방')
    expect(wrapper.get('[data-testid="emerging-stacks"]').classes()).toContain('sm:grid-cols-2')
    expect(wrapper.text()).toContain('Supabase 34%')
    expect(wrapper.text()).toContain('DeepSeek 가성비 1위')
    expect(wrapper.get('[data-testid="maker-opportunities"]').text()).toContain(
      '이번 주 주목해야 할 AI 프로덕트 기회',
    )
  })

  it('navigates a ranked trend to an existing project detail page', async () => {
    await router.push('/projects/prj_301')
    await router.isReady()
    const wrapper = mount(
      { template: '<RouterView />' },
      { global: { plugins: [router, createPinia()] } },
    )

    expect(wrapper.text()).toContain('DevFlow Analytics')
  })
})
