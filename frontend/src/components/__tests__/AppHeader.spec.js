import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/composables/useTheme', () => ({
  useTheme: () => ({ mode: 'light', cycleMode: vi.fn() }),
}))

import AppHeader from '@/components/layout/AppHeader.vue'
import router from '@/router'

describe('AppHeader', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('constrains the desktop search trigger to the Figma header width', () => {
    const wrapper = mount(AppHeader, {
      global: {
        plugins: [createPinia(), router],
        stubs: {
          SearchBar: { template: '<div data-testid="search-bar" />' },
        },
      },
    })

    expect(wrapper.get('[data-testid="search-bar"]').element.parentElement.className).toContain(
      'lg:w-[462px]',
    )
    wrapper.unmount()
  })
})
