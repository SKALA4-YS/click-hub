import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const api = vi.hoisted(() => ({ searchProjects: vi.fn(), getCategories: vi.fn() }))
vi.mock('@/api/search', () => ({ searchProjects: api.searchProjects }))
vi.mock('@/api/catalog', () => ({ getCategories: api.getCategories }))

import SearchBar from '@/components/layout/SearchBar.vue'

describe('SearchBar', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    api.getCategories
      .mockReset()
      .mockResolvedValue([{ id: 'c1', slug: 'developer-tools', name: '개발자 도구' }])
    api.searchProjects.mockReset().mockResolvedValue({
      items: [
        { id: 'p1', title: 'DevFlow Analytics', categoryName: '개발자 도구' },
        { id: 'p2', title: 'Developer Notes', categoryName: '개발자 도구' },
      ],
      nextCursor: null,
      hasNext: false,
    })
  })
  afterEach(() => {
    vi.useRealTimers()
  })

  async function search(wrapper, value) {
    await wrapper.get('input[type="search"]').setValue(value)
    await vi.advanceTimersByTimeAsync(200)
    await flushPromises()
  }

  it('queries the backend and renders project suggestions', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    await search(wrapper, 'Dev')
    expect(api.searchProjects).toHaveBeenCalledWith({ q: 'Dev', category: undefined })
    expect(wrapper.findAll('[role="option"]')).toHaveLength(2)
    expect(wrapper.text()).toContain('DevFlow Analytics')
    wrapper.unmount()
  })

  it('selects the active API result with the keyboard', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    const input = wrapper.get('input[type="search"]')
    await search(wrapper, 'Dev')
    await input.trigger('keydown', { key: 'ArrowDown' })
    await input.trigger('keydown', { key: 'Enter' })
    expect(input.element.value).toBe('DevFlow Analytics')
    expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
    wrapper.unmount()
  })

  it('loads category filters and includes the selected category', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    await flushPromises()
    await wrapper.get('input[type="search"]').trigger('focus')
    await wrapper
      .findAll('button')
      .find((button) => button.text().includes('상세 필터 조건'))
      .trigger('click')
    await wrapper.get('select').setValue('developer-tools')
    await search(wrapper, 'Vue')
    expect(api.searchProjects).toHaveBeenLastCalledWith({ q: 'Vue', category: 'developer-tools' })
    wrapper.unmount()
  })

  it('closes on Escape and restores focus', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    const input = wrapper.get('input[type="search"]')
    await input.trigger('focus')
    await input.trigger('keydown', { key: 'Escape' })
    await flushPromises()
    expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
    expect(document.activeElement).toBe(input.element)
    wrapper.unmount()
  })

  it('shows API failures without fixture fallback', async () => {
    api.searchProjects.mockRejectedValue(new Error('검색 연결 실패'))
    const wrapper = mount(SearchBar)
    await search(wrapper, 'error')
    expect(wrapper.get('[role="alert"]').text()).toBe('검색 연결 실패')
  })
})
