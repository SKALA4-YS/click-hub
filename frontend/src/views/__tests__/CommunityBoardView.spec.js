import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import CommunityBoardView from '@/views/CommunityBoardView.vue'

const api = vi.hoisted(() => ({
  getCommunityBoards: vi.fn(),
  getCommunityPosts: vi.fn(),
  createCommunityPost: vi.fn(),
}))
vi.mock('@/api/community', () => api)

const routerLinkStub = { props: ['to'], template: '<a :href="to"><slot /></a>' }

describe('CommunityBoardView', () => {
  beforeEach(() => {
    api.getCommunityBoards.mockReset().mockResolvedValue([
      { id: 'board-1', slug: 'free', name: '자유게시판' },
      { id: 'board-2', slug: 'qna', name: 'IT / Q&A' },
    ])
    api.getCommunityPosts.mockReset().mockResolvedValue({
      items: [
        {
          id: 'post-1',
          title: '실제 커뮤니티 글',
          authorName: '김메이커',
          viewCount: 4,
          createdAt: '2026-09-04T00:00:00Z',
        },
      ],
      nextCursor: 'next-page',
      hasNext: true,
    })
    api.createCommunityPost.mockReset().mockResolvedValue({ id: 'post-2' })
  })

  it('loads boards and posts from the backend', async () => {
    const wrapper = mount(CommunityBoardView, { global: { stubs: { RouterLink: routerLinkStub } } })
    await flushPromises()

    expect(wrapper.get('h1').text()).toBe('커뮤니티 게시판')
    expect(wrapper.text()).toContain('자유게시판')
    expect(wrapper.text()).toContain('실제 커뮤니티 글')
    expect(api.getCommunityPosts).toHaveBeenCalledWith('free', { cursor: undefined })
  })

  it('creates a post and reloads the active board', async () => {
    const wrapper = mount(CommunityBoardView, { global: { stubs: { RouterLink: routerLinkStub } } })
    await flushPromises()

    await wrapper.get('button').trigger('click')
    await wrapper.get('[name="post-title"]').setValue('새 게시글')
    await wrapper.get('[name="post-body"]').setValue('서버에 저장할 내용')
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(api.createCommunityPost).toHaveBeenCalledWith('free', {
      title: '새 게시글',
      body: '서버에 저장할 내용',
    })
    expect(api.getCommunityPosts).toHaveBeenCalledTimes(2)
  })

  it('loads the next cursor page', async () => {
    const wrapper = mount(CommunityBoardView, { global: { stubs: { RouterLink: routerLinkStub } } })
    await flushPromises()

    await wrapper
      .findAll('button')
      .find((button) => button.text() === '더 보기')
      .trigger('click')
    await flushPromises()

    expect(api.getCommunityPosts).toHaveBeenLastCalledWith('free', { cursor: 'next-page' })
  })
})
