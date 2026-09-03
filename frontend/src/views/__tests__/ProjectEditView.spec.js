import { flushPromises, mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const api = vi.hoisted(() => ({
  getProject: vi.fn(),
  updateProject: vi.fn(),
  getCategories: vi.fn(),
  getTechnologies: vi.fn(),
}))
vi.mock('@/api/projects', () => ({ getProject: api.getProject, updateProject: api.updateProject }))
vi.mock('@/api/catalog', () => ({
  getCategories: api.getCategories,
  getTechnologies: api.getTechnologies,
}))

import ProjectEditView from '@/views/ProjectEditView.vue'

describe('ProjectEditView', () => {
  beforeEach(() => {
    api.getProject.mockReset().mockResolvedValue({
      id: 'project-1',
      title: '기존 프로젝트',
      description: '기존 설명',
      siteUrl: 'https://example.com',
      repositoryUrl: null,
      pricing: 'FREE',
      categorySlug: 'developer-tools',
      tags: ['MVP'],
      thumbnailUrl: null,
      techStacks: [{ technologySlug: 'vue-js' }],
    })
    api.getCategories
      .mockReset()
      .mockResolvedValue([{ id: 'c1', slug: 'developer-tools', name: '개발자 도구' }])
    api.getTechnologies
      .mockReset()
      .mockResolvedValue([{ id: 't1', slug: 'vue-js', name: 'Vue.js' }])
    api.updateProject.mockReset().mockResolvedValue({ id: 'project-1', status: 'DRAFT' })
  })

  it('loads and saves the owned project through the API', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/mypage', component: { template: '<div />' } },
        { path: '/projects/:id/edit', component: ProjectEditView },
      ],
    })
    await router.push('/projects/project-1/edit')
    await router.isReady()
    const wrapper = mount(ProjectEditView, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.get('input').element.value).toBe('기존 프로젝트')
    await wrapper.get('input').setValue('수정 프로젝트')
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(api.updateProject).toHaveBeenCalledWith(
      'project-1',
      expect.objectContaining({ title: '수정 프로젝트', categorySlug: 'developer-tools' }),
    )
    expect(wrapper.get('[role="status"]').text()).toBe('저장되었습니다.')
  })
})
