import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import HomeView from '@/views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const routerLinkStub = {
  props: ['to'],
  template: '<a :href="typeof to === \'string\' ? to : to.path"><slot /></a>',
}

function mountHome() {
  const pinia = createPinia()
  setActivePinia(pinia)

  return mount(HomeView, {
    global: {
      plugins: [pinia],
      stubs: {
        RouterLink: routerLinkStub,
      },
    },
  })
}

async function mountHomeShell() {
  const pinia = createPinia()
  setActivePinia(pinia)
  vi.stubGlobal('localStorage', {
    getItem: () => null,
    setItem: () => {},
  })
  const { default: DefaultLayout } = await import('@/layouts/DefaultLayout.vue')

  return mount(DefaultLayout, {
    global: {
      plugins: [pinia],
      stubs: {
        RouterLink: routerLinkStub,
        RouterView: HomeView,
      },
    },
  })
}

describe('HomeView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    document.documentElement.classList.remove('dark')
  })

  it('shows its feed in the Figma section order', () => {
    const wrapper = mountHome()

    expect(wrapper.findAll('h2').map((heading) => heading.text())).toEqual([
      'Top 100',
      '맞춤 추천',
      '내가 팔로잉한 개발자',
    ])
  })

  it('filters every feed section when a category tab is selected', async () => {
    const wrapper = mountHome()
    useAuthStore().$patch({ user: { display_name: '김민준' } })

    await wrapper.get('button[aria-label="개발도구 카테고리"]').trigger('click')

    expect(wrapper.findAll('h3')).toHaveLength(9)
    expect(wrapper.findAll('h3').every((heading) => heading.text() === 'DevFlow Analytics')).toBe(
      true,
    )
  })

  it('exposes categories as a pressed button group instead of incomplete tabs', async () => {
    const wrapper = mountHome()
    const designCategory = wrapper.get('button[aria-label="디자인 카테고리"]')

    expect(wrapper.get('[aria-label="프로젝트 카테고리"]').attributes('role')).toBe('group')
    expect(
      wrapper.findAll('button[aria-label$="카테고리"]').map((button) => button.text()),
    ).toEqual(['전체', '개발도구', '디자인', '엔터테인먼트', 'AI', '생산성', '마케팅', '기타'])
    expect(designCategory.attributes('role')).toBeUndefined()
    expect(designCategory.attributes('aria-pressed')).toBe('false')

    await designCategory.trigger('click')

    expect(designCategory.attributes('aria-pressed')).toBe('true')
  })

  it('links every visible project card to its project detail route', async () => {
    const wrapper = mountHome()
    useAuthStore().$patch({ user: { display_name: '김민준' } })
    await wrapper.vm.$nextTick()

    expect(
      wrapper.findAll('a[href^="/projects/prj_"]').map((link) => link.attributes('href')),
    ).toEqual([
      '/projects/prj_301',
      '/projects/prj_302',
      '/projects/prj_303',
      '/projects/prj_304',
      '/projects/prj_305',
      '/projects/prj_306',
      '/projects/prj_307',
      '/projects/prj_308',
      '/projects/prj_309',
    ])
  })

  it('renders the supplied Figma Home card copy and neutral thumbnail placeholders', async () => {
    const wrapper = mountHome()
    useAuthStore().$patch({ user: { display_name: '김민준' } })
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('h3').map((heading) => heading.text())).toEqual(
      Array(9).fill('DevFlow Analytics'),
    )
    expect(wrapper.text()).toContain(
      'Real-time performance tracking for indie makers. Monitor your server load, user engagement, and error...',
    )
    expect(wrapper.text()).toContain('저장 1,204')
    expect(wrapper.text()).toContain('댓글 20')
    expect(wrapper.text()).toContain('조회 20')
    expect(wrapper.findAll('.bg-gradient-to-br')).toHaveLength(0)
  })

  it('replaces the signed-out following prompt with followed projects after login', async () => {
    const wrapper = mountHome()
    const auth = useAuthStore()

    expect(wrapper.text()).toContain('로그인하면 구독한 제작자가')

    auth.$patch({ user: { display_name: '김민준' } })
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('DevFlow Analytics')
    expect(wrapper.text()).not.toContain('로그인하면 구독한 제작자가')
  })

  it('keeps the home content visible while the shared search overlay is open in light and dark mode', async () => {
    const wrapper = await mountHomeShell()
    const themeButton = wrapper.get('button[title^="테마:"]')

    await wrapper.get('input[aria-label="통합 검색"]').trigger('focus')

    expect(wrapper.text()).toContain('Top 100')
    expect(wrapper.get('[role="dialog"][aria-label="검색 제안"]')).toBeTruthy()
    expect(document.documentElement.classList.contains('dark')).toBe(false)

    await themeButton.trigger('click')

    expect(document.documentElement.classList.contains('dark')).toBe(true)
    expect(wrapper.text()).toContain('맞춤 추천')
    expect(wrapper.get('[role="dialog"][aria-label="검색 제안"]')).toBeTruthy()
  })
})
