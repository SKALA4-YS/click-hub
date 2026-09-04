import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import HomeView from '@/views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const api = vi.hoisted(() => ({
  getFeed: vi.fn(),
  getMySubscriptions: vi.fn(),
  getCreator: vi.fn(),
}))

vi.mock('@/api/feed', () => ({ getFeed: api.getFeed }))
vi.mock('@/api/users', () => ({
  getMySubscriptions: api.getMySubscriptions,
  getCreator: api.getCreator,
  updateOnboarding: vi.fn(),
  updateProfile: vi.fn(),
}))

const routerLinkStub = {
  props: ['to'],
  template: '<a :href="typeof to === \'string\' ? to : to.path"><slot /></a>',
}

const projects = [
  {
    id: 'project-1',
    title: '실제 프로젝트 A',
    description: 'Backend 피드에서 받은 프로젝트',
    categorySlug: 'developer-tools',
    categoryName: '개발자 도구',
    likeCount: 12,
  },
  {
    id: 'project-2',
    title: '실제 프로젝트 B',
    description: 'PostgreSQL에 저장된 프로젝트',
    categorySlug: 'ai-service',
    categoryName: 'AI 서비스',
    likeCount: 7,
  },
]

function mountHome() {
  return mount(HomeView, {
    global: { plugins: [createPinia()], stubs: { RouterLink: routerLinkStub } },
  })
}

describe('HomeView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    api.getFeed.mockReset().mockResolvedValue({ items: projects, nextCursor: null, hasNext: false })
    api.getMySubscriptions.mockReset().mockResolvedValue([])
    api.getCreator.mockReset()
  })

  it('renders the latest backend feed without a project ranking section', async () => {
    const wrapper = mountHome()
    await flushPromises()

    expect(wrapper.findAll('h2').map((heading) => heading.text())).toContain('최신 프로젝트')
    expect(wrapper.text()).not.toContain('Top 100')
    expect(wrapper.text()).not.toContain('HOT')
    expect(
      wrapper
        .findAll('.grid h3')
        .map((heading) => heading.text())
        .slice(0, 2),
    ).toEqual(['실제 프로젝트 A', '실제 프로젝트 B'])
    expect(wrapper.text()).toContain('Backend 피드에서 받은 프로젝트')
    expect(api.getFeed).toHaveBeenCalledOnce()
  })

  it('filters API results by the selected category', async () => {
    const wrapper = mountHome()
    await flushPromises()

    await wrapper.get('button[aria-label="AI 카테고리"]').trigger('click')

    expect(wrapper.text()).toContain('실제 프로젝트 B')
    expect(wrapper.text()).not.toContain('실제 프로젝트 A')
  })

  it('loads followed creators projects for authenticated users', async () => {
    const wrapper = mountHome()
    const auth = useAuthStore()
    api.getMySubscriptions.mockResolvedValue([{ id: 'creator-1' }])
    api.getCreator.mockResolvedValue({
      projects: [{ ...projects[0], id: 'followed-project', title: '구독 프로젝트' }],
    })

    auth.$patch({ user: { id: 'viewer' } })
    await flushPromises()

    expect(api.getMySubscriptions).toHaveBeenCalledOnce()
    expect(api.getCreator).toHaveBeenCalledWith('creator-1')
    expect(wrapper.text()).toContain('구독 프로젝트')
  })

  it('shows a retryable API error', async () => {
    api.getFeed.mockRejectedValue(new Error('피드 연결 실패'))
    const wrapper = mountHome()
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toBe('피드 연결 실패')
    expect(wrapper.text()).toContain('다시 시도')
  })
})
