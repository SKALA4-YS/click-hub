import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const api = vi.hoisted(() => ({
  getCategories: vi.fn(),
  getProjectRankings: vi.fn(),
  searchProjects: vi.fn(),
}))

vi.mock('vue-router', () => ({
  RouterLink: { props: ['to'], template: '<a :href="to"><slot /></a>' },
  useRoute: () => ({ query: {} }),
}))
vi.mock('@/api/catalog', () => ({ getCategories: api.getCategories }))
vi.mock('@/api/rankings', () => ({ getProjectRankings: api.getProjectRankings }))
vi.mock('@/api/search', () => ({ searchProjects: api.searchProjects }))

import ProjectListView from '@/views/ProjectListView.vue'

const projects = [
  {
    id: 'project-1',
    title: '프로젝트 A',
    description: '첫 번째 프로젝트',
    categorySlug: 'developer-tools',
    categoryName: '개발자 도구',
    publishedAt: '2026-09-01T00:00:00Z',
  },
  {
    id: 'project-2',
    title: '프로젝트 B',
    description: '두 번째 프로젝트',
    categorySlug: 'ai-service',
    categoryName: 'AI 서비스',
    publishedAt: '2026-09-03T00:00:00Z',
  },
]

function mountProjectList() {
  return mount(ProjectListView, {
    global: {
      stubs: { RouterLink: { props: ['to'], template: '<a :href="to"><slot /></a>' } },
    },
  })
}

describe('ProjectListView', () => {
  beforeEach(() => {
    api.getCategories.mockReset().mockResolvedValue([
      { id: 'category-1', slug: 'developer-tools', name: '개발자 도구' },
      { id: 'category-2', slug: 'ai-service', name: 'AI 서비스' },
    ])
    api.getProjectRankings.mockReset().mockResolvedValue([
      { rank: 1, projectId: 'project-1', score: 10 },
      { rank: 2, projectId: 'project-2', score: 8 },
    ])
    api.searchProjects.mockReset().mockResolvedValue({
      items: projects,
      nextCursor: null,
      hasNext: false,
    })
  })

  it('renders catalog and project data returned by the backend', async () => {
    const wrapper = mountProjectList()
    await flushPromises()

    expect(wrapper.findAll('.project-grid h3').map((card) => card.text())).toEqual([
      '프로젝트 A',
      '프로젝트 B',
    ])
    expect(wrapper.get('[aria-label="프로젝트 수"]').text()).toBe('2개 프로젝트')
    expect(wrapper.get('button[aria-label="AI 서비스 카테고리"]')).toBeTruthy()
  })

  it('reloads search results using the selected catalog category', async () => {
    const wrapper = mountProjectList()
    await flushPromises()
    api.searchProjects.mockClear()

    await wrapper.get('button[aria-label="AI 서비스 카테고리"]').trigger('click')
    await flushPromises()

    expect(api.searchProjects).toHaveBeenCalledWith({ category: 'ai-service', cursor: undefined })
  })

  it('sorts loaded API results by publication date', async () => {
    const wrapper = mountProjectList()
    await flushPromises()

    await wrapper.get('[aria-label="프로젝트 정렬"]').setValue('latest')

    expect(wrapper.findAll('.project-grid h3').map((card) => card.text())).toEqual([
      '프로젝트 B',
      '프로젝트 A',
    ])
  })

  it('appends the next cursor page', async () => {
    api.searchProjects
      .mockResolvedValueOnce({ items: [projects[0]], nextCursor: 'next', hasNext: true })
      .mockResolvedValueOnce({ items: [projects[1]], nextCursor: null, hasNext: false })
    const wrapper = mountProjectList()
    await flushPromises()

    await wrapper
      .findAll('button')
      .find((button) => button.text() === '더 보기')
      .trigger('click')
    await flushPromises()

    expect(api.searchProjects).toHaveBeenLastCalledWith({ category: null, cursor: 'next' })
    expect(wrapper.findAll('.project-grid h3')).toHaveLength(2)
  })
})
