import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import AdminProjectDetailView from '@/views/AdminProjectDetailView.vue'

const api = vi.hoisted(() => ({
  getAdminProjectDetail: vi.fn(),
  approveProject: vi.fn(),
  rejectProject: vi.fn(),
}))
vi.mock('@/api/admin', () => api)

const replace = vi.hoisted(() => vi.fn())

vi.mock('vue-router', () => ({
  useRouter: () => ({ replace }),
  useRoute: () => ({ params: { id: 'project-1' } }),
  RouterLink: { props: ['to'], template: '<a :href="to"><slot /></a>' },
}))

function detailFixture(overrides = {}) {
  return {
    id: 'project-1',
    title: '검토 대기 프로젝트',
    description: '실제 서비스 설명입니다.',
    siteUrl: 'https://example.com',
    repositoryUrl: 'https://github.com/example/example',
    pricing: 'FREE',
    status: 'PENDING_REVIEW',
    categorySlug: 'developer-tools',
    categoryName: '개발자 도구',
    tags: ['vue', 'spring'],
    thumbnailUrl: null,
    screenshots: [],
    techStacks: [
      { technologyName: 'Vue', technologySlug: 'vue', group: 'FRONTEND', version: null },
    ],
    ownerName: '김메이커',
    ownerId: 'owner-1',
    publishedAt: null,
    likeCount: 0,
    favoriteCount: 0,
    likedByMe: false,
    favoritedByMe: false,
    ...overrides,
  }
}

describe('AdminProjectDetailView', () => {
  beforeEach(() => {
    replace.mockReset()
    api.getAdminProjectDetail.mockReset().mockResolvedValue(detailFixture())
    api.approveProject.mockReset().mockResolvedValue({ id: 'project-1', status: 'PUBLISHED' })
    api.rejectProject.mockReset().mockResolvedValue({ id: 'project-1', status: 'REJECTED' })
  })

  it('shows the submitted project like the registration form did', async () => {
    const wrapper = mount(AdminProjectDetailView)
    await flushPromises()

    expect(api.getAdminProjectDetail).toHaveBeenCalledWith('project-1')
    expect(wrapper.get('h1').text()).toBe('검토 대기 프로젝트')
    expect(wrapper.text()).toContain('개발자 도구')
    expect(wrapper.text()).toContain('무료')
    expect(wrapper.text()).toContain('Vue')
    expect(wrapper.text()).toContain('#vue')
  })

  it('approves the project and shows a confirmation message', async () => {
    const wrapper = mount(AdminProjectDetailView)
    await flushPromises()

    await wrapper
      .findAll('button')
      .find((button) => button.text() === '승인')
      .trigger('click')
    await flushPromises()

    expect(api.approveProject).toHaveBeenCalledWith('project-1')
    expect(wrapper.get('[role="status"]').text()).toContain('승인되었습니다')
  })

  it('rejects the project once a reason is entered', async () => {
    const wrapper = mount(AdminProjectDetailView)
    await flushPromises()

    await wrapper
      .findAll('button')
      .find((button) => button.text() === '거절')
      .trigger('click')
    await wrapper.get('input').setValue('스크린샷이 실제 화면과 다릅니다.')
    await wrapper
      .findAll('button')
      .find((button) => button.text() === '거절 확정')
      .trigger('click')
    await flushPromises()

    expect(api.rejectProject).toHaveBeenCalledWith('project-1', '스크린샷이 실제 화면과 다릅니다.')
    expect(wrapper.get('[role="status"]').text()).toContain('거절되었습니다')
  })

  it('hides the approve/reject actions once a project is no longer pending', async () => {
    api.getAdminProjectDetail.mockResolvedValue(detailFixture({ status: 'PUBLISHED' }))

    const wrapper = mount(AdminProjectDetailView)
    await flushPromises()

    expect(wrapper.findAll('button').find((button) => button.text() === '승인')).toBeUndefined()
  })

  it('redirects to the home page when the backend forbids access', async () => {
    api.getAdminProjectDetail.mockReset().mockRejectedValue({
      message: '접근 권한이 없습니다.',
      status: 403,
    })

    mount(AdminProjectDetailView)
    await flushPromises()

    expect(replace).toHaveBeenCalledWith('/')
  })
})
