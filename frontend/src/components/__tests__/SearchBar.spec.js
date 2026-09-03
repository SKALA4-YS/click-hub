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
    expect(wrapper.findAll('input[type="search"]')).toHaveLength(1)
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

  it('moves through suggestions and selects the active option from the combobox', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    const input = wrapper.get('input[type="search"]')
    await input.setValue('Dev')

    await input.trigger('keydown', { key: 'ArrowDown' })
    expect(wrapper.get('[role="option"][aria-selected="true"]').text()).toContain('DevFlow')

    await input.trigger('keydown', { key: 'ArrowUp' })
    expect(wrapper.find('[role="option"][aria-selected="true"]').exists()).toBe(false)

    await input.trigger('keydown', { key: 'ArrowDown' })
    await input.trigger('keydown', { key: 'Enter' })
    expect(input.element.value).toBe('DevFlow Analytics')
    expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
    wrapper.unmount()
  })

  it('does not steal focus when an outside click closes the overlay', async () => {
    const outsideButton = document.createElement('button')
    document.body.append(outsideButton)
    const wrapper = mount(SearchBar, { attachTo: document.body })
    await wrapper.get('input[type="search"]').trigger('focus')
    outsideButton.focus()

    await outsideButton.dispatchEvent(new MouseEvent('click', { bubbles: true }))

    expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
    expect(document.activeElement).toBe(outsideButton)
    wrapper.unmount()
    outsideButton.remove()
  })

  it('clamps a previously active option before selecting after the query narrows', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    const input = wrapper.get('input[type="search"]')
    await input.setValue('개발자 도구')

    await input.trigger('keydown', { key: 'ArrowDown' })
    await input.trigger('keydown', { key: 'ArrowDown' })
    await input.trigger('keydown', { key: 'ArrowDown' })
    await input.trigger('keydown', { key: 'ArrowDown' })
    await input.setValue('Dev')

    await input.trigger('keydown', { key: 'Enter' })

    expect(input.element.value).toBe('Dev')
    expect(wrapper.find('[role="dialog"]').exists()).toBe(true)
    wrapper.unmount()
  })
})
