import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import SearchBar from '@/components/layout/SearchBar.vue'

describe('SearchBar', () => {
  it('uses the Figma desktop overlay dimensions and one contextual end control', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    const input = wrapper.get('input[type="search"]')

    await input.setValue('데이터 자격')

    const overlay = wrapper.get('[role="dialog"]')
    expect(overlay.classes()).toEqual(expect.arrayContaining(['lg:w-[503px]', 'lg:h-[227.9px]']))
    expect(wrapper.findAll('[aria-label="검색어 지우기"]')).toHaveLength(1)
    expect(wrapper.find('[aria-label="검색 닫기"]').exists()).toBe(false)
    wrapper.unmount()
  })

  it('shows the three Figma data-qualification suggestions with their exported assets', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    const input = wrapper.get('input[type="search"]')

    await input.setValue('데이터 자격')

    const options = wrapper.findAll('[role="option"]')
    expect(options.map((option) => option.text())).toEqual([
      '데이터 자격증 SQLD',
      '데이터 자격증 ADsP',
      '데이터 자격 공부 방법',
    ])
    for (const option of options) {
      expect(option.findAll('img')).toHaveLength(2)
      expect(option.find('img').classes()).toContain('suggestion-search-icon')
      expect(option.findAll('img')[1].classes()).toContain('suggestion-arrow-icon')
    }
    wrapper.unmount()
  })

  it('uses distinct Figma surface and search-icon states when the combobox expands', async () => {
    const wrapper = mount(SearchBar, { attachTo: document.body })
    const input = wrapper.get('input[type="search"]')
    const closedSurface = wrapper.get('div.relative > div')

    expect(closedSurface.classes()).toContain('rounded-full')
    expect(closedSurface.classes()).not.toContain('rounded-[17px]')
    expect(closedSurface.classes()).not.toContain('p-0')
    expect(closedSurface.get('img').classes()).toEqual(expect.arrayContaining(['h-4', 'w-4']))

    await input.trigger('focus')

    const openSurface = wrapper.get('[role="dialog"]')
    expect(openSurface.classes()).toContain('rounded-[17px]')
    expect(openSurface.classes()).not.toContain('rounded-full')
    expect(openSurface.classes()).toContain('p-0')
    expect(openSurface.classes()).not.toContain('px-4')
    expect(openSurface.classes()).not.toContain('py-2')
    expect(openSurface.get('img').classes()).toEqual(
      expect.arrayContaining(['h-[19.787px]', 'w-[20.828px]']),
    )
    wrapper.unmount()
  })

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
