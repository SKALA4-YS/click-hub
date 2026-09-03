import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.hoisted(() => {
  Object.defineProperty(globalThis, 'localStorage', {
    value: { getItem: () => null, setItem: () => {}, removeItem: () => {} },
    configurable: true,
  })
})

import FavoritesView from '@/views/FavoritesView.vue'
import FollowingView from '@/views/FollowingView.vue'
import MyPageView from '@/views/MyPageView.vue'
import { useAuthStore } from '@/stores/auth'

const routerLinkStub = {
  props: ['to'],
  template:
    '<a :href="typeof to === \'string\' ? to : to?.path || `/projects/${to?.params?.id}`"><slot /></a>',
}

function mountView(component) {
  return mount(component, { global: { stubs: { RouterLink: routerLinkStub } } })
}

describe('member library pages', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    useAuthStore().$patch({ user: { display_name: '김민준' } })
  })

  it('uses the full maker dashboard for my page', () => {
    const wrapper = mountView(MyPageView)
    expect(wrapper.text()).toContain('김민준')
    expect(wrapper.text()).toContain('웹 클릭수')
    expect(wrapper.text()).toContain('내 등록 프로젝트')
    expect(wrapper.text()).toContain('마이 퀵 메뉴')
  })

  it('shows the Figma favorite vault and filters saved projects', async () => {
    const wrapper = mountView(FavoritesView)
    expect(wrapper.get('h1').text()).toBe('즐겨찾기 보관함')
    expect(wrapper.text()).toContain('18개 저장됨')
    expect(wrapper.findAll('[data-testid="favorite-card"]')).toHaveLength(6)

    await wrapper.get('input[name="favorite-search"]').setValue('CodeSnap')
    expect(wrapper.findAll('[data-testid="favorite-card"]')).toHaveLength(1)
    expect(wrapper.text()).toContain('CodeSnap Pro')
  })

  it('shows following summaries and lets a member unfollow locally', async () => {
    const wrapper = mountView(FollowingView)
    expect(wrapper.get('h1').text()).toContain('내 팔로잉 관리')
    expect(wrapper.text()).toContain('팔로잉 메이커')
    expect(wrapper.text()).toContain('14명')
    expect(wrapper.text()).toContain('구독 알림 설정')
    expect(wrapper.text()).toContain('추천 인디 메이커')
    expect(wrapper.findAll('[data-testid="following-card"]')).toHaveLength(5)

    await wrapper.get('button[aria-label="김민준 팔로우 해제"]').trigger('click')
    expect(wrapper.findAll('[data-testid="following-card"]')).toHaveLength(4)
  })
})
