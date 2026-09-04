import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import AdminApprovalView from '@/views/AdminApprovalView.vue'

const api = vi.hoisted(() => ({
  getPendingProjects: vi.fn(),
  approveProject: vi.fn(),
  rejectProject: vi.fn(),
}))
vi.mock('@/api/admin', () => api)

const replace = vi.hoisted(() => vi.fn())

vi.mock('vue-router', () => ({
  useRouter: () => ({ replace }),
  RouterLink: { props: ['to'], template: '<a :href="to"><slot /></a>' },
}))

describe('AdminApprovalView', () => {
  beforeEach(() => {
    replace.mockReset()
    api.getPendingProjects.mockReset().mockResolvedValue([
      {
        id: 'project-1',
        title: '검토 대기 프로젝트',
        description: '실제 서비스 설명입니다.',
        siteUrl: 'https://example.com',
        status: 'PENDING_REVIEW',
        ownerName: '김메이커',
        createdAt: '2026-09-01T00:00:00Z',
      },
    ])
    api.approveProject.mockReset().mockResolvedValue({ id: 'project-1', status: 'PUBLISHED' })
    api.rejectProject.mockReset().mockResolvedValue({ id: 'project-1', status: 'REJECTED' })
  })

  it('loads only pending-review projects from the backend', async () => {
    const wrapper = mount(AdminApprovalView)
    await flushPromises()

    expect(wrapper.get('h1').text()).toBe('게시물 승인 관리')
    expect(wrapper.text()).toContain('검토 대기 프로젝트')
    expect(wrapper.text()).toContain('미정')
    expect(wrapper.text()).toContain('김메이커')
    expect(api.getPendingProjects).toHaveBeenCalled()
  })

  it('approves a project and removes it from the list', async () => {
    const wrapper = mount(AdminApprovalView)
    await flushPromises()

    await wrapper.get('button').trigger('click')
    await flushPromises()

    expect(api.approveProject).toHaveBeenCalledWith('project-1')
    expect(wrapper.text()).not.toContain('검토 대기 프로젝트')
  })

  it('rejects a project with the entered reason', async () => {
    const wrapper = mount(AdminApprovalView)
    await flushPromises()

    await wrapper.findAll('button')[1].trigger('click')
    await wrapper.get('input').setValue('스크린샷이 실제 화면과 다릅니다.')
    await wrapper
      .findAll('button')
      .find((button) => button.text() === '거절 확정')
      .trigger('click')
    await flushPromises()

    expect(api.rejectProject).toHaveBeenCalledWith('project-1', '스크린샷이 실제 화면과 다릅니다.')
    expect(wrapper.text()).not.toContain('검토 대기 프로젝트')
  })

  it('redirects to the home page when the backend forbids access', async () => {
    api.getPendingProjects
      .mockReset()
      .mockRejectedValue({ message: '접근 권한이 없습니다.', status: 403 })

    mount(AdminApprovalView)
    await flushPromises()

    expect(replace).toHaveBeenCalledWith('/')
  })
})
