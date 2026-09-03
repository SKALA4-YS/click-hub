import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import DeveloperRankingView from '@/views/DeveloperRankingView.vue'

const routerLinkStub = {
  props: ['to'],
  template: '<a :href="typeof to === \'string\' ? to : to.path"><slot /></a>',
}

function mountView() {
  return mount(DeveloperRankingView, {
    global: { stubs: { RouterLink: routerLinkStub } },
  })
}

describe('DeveloperRankingView', () => {
  it('shows the Figma ranking sections and developer profile links', () => {
    const wrapper = mountView()

    expect(wrapper.text()).toContain('개발자 랭킹 Top Indie Makers')
    expect(wrapper.text()).toContain('주간 명예의 전당 (Top 3 Podium)')
    expect(wrapper.text()).toContain('순위 리스트 (4위 ~ 10위)')
    const profilePaths = new Set(
      wrapper.findAll('a[href^="/developers/"]').map((link) => link.attributes('href')),
    )
    expect(profilePaths.size).toBe(10)
  })

  it('filters the ranking list by selected field', async () => {
    const wrapper = mountView()

    await wrapper.get('button[aria-label^="AI"]').trigger('click')

    expect(wrapper.get('button[aria-label^="AI"]').attributes('aria-pressed')).toBe('true')
    expect(wrapper.text()).toContain('PromptCraft Studio')
    expect(wrapper.text()).not.toContain('FlowBoard Kanban')
  })

  it('changes the selected ranking period', async () => {
    const wrapper = mountView()

    await wrapper.get('button[aria-label="월간 랭킹"]').trigger('click')

    expect(wrapper.get('button[aria-label="월간 랭킹"]').attributes('aria-pressed')).toBe('true')
    expect(wrapper.text()).toContain('월간 랭킹')
  })
})
