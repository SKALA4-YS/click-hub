import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import SearchBar from '@/components/layout/SearchBar.vue'

describe('SearchBar', () => {
  it('opens its search overlay when the input receives focus', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })

    await wrapper.get('input[type="search"]').trigger('focus')

    expect(wrapper.get('[role="dialog"]').attributes('aria-label')).toBe('검색 제안')
    expect(wrapper.get('input[type="search"]').attributes('role')).toBe('combobox')
    expect(wrapper.get('input[type="search"]').attributes('aria-expanded')).toBe('true')
    expect(wrapper.get('[role="listbox"]')).toBeTruthy()
    wrapper.unmount()
  })

  it('closes its search overlay with the close control', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    await wrapper.get('input[type="search"]').trigger('focus')

    const input = wrapper.get('input[type="search"]')
    const closeButton = wrapper.get('[aria-label="검색 닫기"]')
    closeButton.element.focus()
    await closeButton.trigger('click')

    expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
    expect(document.activeElement).toBe(input.element)
    wrapper.unmount()
  })

  it('closes the overlay on Escape and returns focus to the input', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    const input = wrapper.get('input[type="search"]')
    await input.trigger('focus')

    await input.trigger('keydown', { key: 'Escape' })

    expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
    expect(document.activeElement).toBe(input.element)
    wrapper.unmount()
  })

  it('closes the overlay when clicking outside', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    await wrapper.get('input[type="search"]').trigger('focus')

    await document.body.dispatchEvent(new MouseEvent('click', { bubbles: true }))

    expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
    wrapper.unmount()
  })
})
