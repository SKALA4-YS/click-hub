import { flushPromises, mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const api = vi.hoisted(() => ({
  getCommunityPost: vi.fn(),
  getCommunityComments: vi.fn(),
  createCommunityComment: vi.fn(),
  updateCommunityPost: vi.fn(),
  deleteCommunityPost: vi.fn(),
}))
vi.mock('@/api/community', () => api)

import CommunityPostDetailView from '@/views/CommunityPostDetailView.vue'

async function mountView() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/community', component: { template: '<div>목록</div>' } },
      { path: '/community/posts/:id', component: CommunityPostDetailView },
    ],
  })
  await router.push('/community/posts/post-1')
  await router.isReady()
  const wrapper = mount(CommunityPostDetailView, { global: { plugins: [router] } })
  await flushPromises()
  return { wrapper, router }
}

describe('CommunityPostDetailView', () => {
  beforeEach(() => {
    api.getCommunityPost.mockReset().mockResolvedValue({
      id: 'post-1',
      title: '서버 게시글',
      body: '본문',
      authorName: 'Maker',
      viewCount: 3,
      mine: true,
    })
    api.getCommunityComments
      .mockReset()
      .mockResolvedValue([{ id: 'comment-1', authorName: 'Reader', body: '첫 댓글' }])
    api.createCommunityComment
      .mockReset()
      .mockResolvedValue({ id: 'comment-2', authorName: 'Maker', body: '새 댓글' })
    api.updateCommunityPost.mockReset().mockResolvedValue(null)
    api.deleteCommunityPost.mockReset().mockResolvedValue(null)
  })

  it('loads a post and persists a comment', async () => {
    const { wrapper } = await mountView()
    expect(wrapper.text()).toContain('서버 게시글')
    expect(wrapper.text()).toContain('첫 댓글')
    await wrapper.get('[aria-label="댓글 내용"]').setValue('새 댓글')
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(api.createCommunityComment).toHaveBeenCalledWith('post-1', {
      body: '새 댓글',
      parentId: null,
    })
    expect(wrapper.text()).toContain('댓글 2개')
  })

  it('updates an owned post', async () => {
    const { wrapper } = await mountView()
    await wrapper
      .findAll('button')
      .find((button) => button.text() === '수정')
      .trigger('click')
    await wrapper.get('[aria-label="게시글 제목 수정"]').setValue('수정된 글')
    await wrapper
      .findAll('button')
      .find((button) => button.text() === '수정 저장')
      .trigger('click')
    await flushPromises()
    expect(api.updateCommunityPost).toHaveBeenCalledWith('post-1', {
      title: '수정된 글',
      body: '본문',
    })
  })
})
