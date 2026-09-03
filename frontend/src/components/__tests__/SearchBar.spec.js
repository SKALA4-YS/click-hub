import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import SearchBar from '@/components/layout/SearchBar.vue'

describe('SearchBar', () => {
  it('opens its search overlay when the input receives focus', async () => {
    const wrapper = mount(SearchBar)

    await wrapper.get('input[type="search"]').trigger('focus')

    expect(wrapper.get('[role="dialog"]').attributes('aria-label')).toBe('검색 제안')
  })

  it('closes its search overlay with the close control', async () => {
    const wrapper = mount(SearchBar)
    await wrapper.get('input[type="search"]').trigger('focus')

    await wrapper.get('[aria-label="검색 닫기"]').trigger('click')

    expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
  })
})
