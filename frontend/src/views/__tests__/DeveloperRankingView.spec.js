import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import DeveloperRankingView from '@/views/DeveloperRankingView.vue'

const getDeveloperRankings = vi.hoisted(() => vi.fn())
vi.mock('@/api/rankings', () => ({ getDeveloperRankings }))

const routerLinkStub = {
  props: ['to'],
  template: '<a :href="typeof to === \'string\' ? to : to.path"><slot /></a>',
}

describe('DeveloperRankingView', () => {
  beforeEach(() => {
    getDeveloperRankings.mockReset().mockResolvedValue([
      { rank: 1, creatorId: 'creator-1', displayName: '김민준', score: 9.25 },
      { rank: 2, creatorId: 'creator-2', displayName: 'Sarah Park', score: 7.1 },
    ])
  })

  it('renders developer rankings returned by the backend', async () => {
    const wrapper = mount(DeveloperRankingView, {
      global: { stubs: { RouterLink: routerLinkStub } },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('개발자 랭킹 Top Indie Makers')
    expect(wrapper.text()).toContain('김민준')
    expect(wrapper.get('a[href="/developers/creator-1"]')).toBeTruthy()
    expect(getDeveloperRankings).toHaveBeenCalledOnce()
  })

  it('filters the loaded ranking by display name', async () => {
    const wrapper = mount(DeveloperRankingView, {
      global: { stubs: { RouterLink: routerLinkStub } },
    })
    await flushPromises()

    await wrapper.get('input[placeholder="메이커 검색..."]').setValue('sarah')

    expect(wrapper.text()).toContain('Sarah Park')
    expect(wrapper.text()).not.toContain('김민준')
  })
})
