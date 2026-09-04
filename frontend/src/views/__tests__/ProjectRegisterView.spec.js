import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const api = vi.hoisted(() => ({
  getCategories: vi.fn(),
  getTechnologies: vi.fn(),
  createProject: vi.fn(),
  submitProject: vi.fn(),
}))
vi.mock('@/api/catalog', () => ({
  getCategories: api.getCategories,
  getTechnologies: api.getTechnologies,
}))
vi.mock('@/api/projects', () => ({
  createProject: api.createProject,
  submitProject: api.submitProject,
}))
vi.mock('@/api/users', () => ({ updateOnboarding: vi.fn(), updateProfile: vi.fn() }))

import ProjectRegisterView from '@/views/ProjectRegisterView.vue'
import { useAuthStore } from '@/stores/auth'

const routerLinkStub = {
  props: ['to'],
  template: '<a :href="typeof to === \'string\' ? to : to?.path"><slot /></a>',
}

describe('ProjectRegisterView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    api.getCategories
      .mockReset()
      .mockResolvedValue([{ id: 'c1', slug: 'developer-tools', name: '개발자 도구' }])
    api.getTechnologies
      .mockReset()
      .mockResolvedValue([{ id: 't1', slug: 'vue-js', name: 'Vue.js', defaultGroup: 'FRONTEND' }])
    api.createProject.mockReset().mockResolvedValue({ id: 'project-id', status: 'DRAFT' })
    api.submitProject.mockReset().mockResolvedValue({ id: 'project-id', status: 'PENDING_REVIEW' })
  })

  function mountView() {
    const pinia = createPinia()
    setActivePinia(pinia)
    useAuthStore().$patch({ user: { id: 'viewer', displayName: '김민준' } })
    return mount(ProjectRegisterView, {
      global: { plugins: [pinia], stubs: { RouterLink: routerLinkStub } },
    })
  }

  it('loads registration catalogs from the backend', async () => {
    const wrapper = mountView()
    await flushPromises()
    expect(wrapper.text()).toContain('개발자 도구')
    expect(wrapper.text()).toContain('Vue.js')
    expect(wrapper.get('button[type="submit"]').text()).toContain('프로젝트 등록 및 검토 요청')
  })

  it('creates a draft and submits it for review', async () => {
    const wrapper = mountView()
    await flushPromises()
    await wrapper.get('[name="title"]').setValue('DevFlow Analytics')
    await wrapper.get('[name="description"]').setValue('배포한 서비스를 분석하는 대시보드입니다.')
    await wrapper.get('[name="siteUrl"]').setValue('https://devflow.example')
    await wrapper.get('[name="category"]').setValue('developer-tools')
    await wrapper.get('[name="agreed"]').setValue(true)
    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(api.createProject).toHaveBeenCalledWith(
      expect.objectContaining({ title: 'DevFlow Analytics', categorySlug: 'developer-tools' }),
    )
    expect(api.submitProject).toHaveBeenCalledWith('project-id')
    expect(wrapper.get('[data-testid="project-registration-success"]')).toBeTruthy()
    expect(wrapper.text()).toContain('PENDING_REVIEW')
    expect(wrapper.get('a[href="/projects/project-id"]')).toBeTruthy()
  })

  it('removes a tag after it was added', async () => {
    const wrapper = mountView()
    await flushPromises()
    const tagInput = wrapper.get('input[placeholder="입력 후 Enter"]')
    await tagInput.setValue('Analytics')
    await tagInput.trigger('keydown.enter')
    expect(wrapper.text()).toContain('#Analytics')

    await wrapper.get('button[aria-label="Analytics 태그 삭제"]').trigger('click')

    expect(wrapper.text()).not.toContain('#Analytics')
  })
})
