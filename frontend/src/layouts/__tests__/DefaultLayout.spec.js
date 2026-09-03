import { nextTick } from 'vue'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import { vi } from 'vitest'

vi.mock('@/components/layout/AppHeader.vue', () => ({
  default: { template: '<header data-testid="app-header" />' },
}))

import DefaultLayout from '@/layouts/DefaultLayout.vue'
import router from '@/router'

async function mountAt(path) {
  await router.push(path)
  await router.isReady()

  return mount(DefaultLayout, {
    global: {
      plugins: [createPinia(), router],
    },
  })
}

describe('DefaultLayout route shells', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it.each(['/login', '/signup', '/onboarding'])(
    'renders %s without the global header or footer',
    async (path) => {
      const wrapper = await mountAt(path)

      expect(router.currentRoute.value.meta.standalone).toBe(true)
      expect(wrapper.find('[data-testid="app-header"]').exists()).toBe(false)
      expect(wrapper.find('[data-testid="app-footer"]').exists()).toBe(false)
      expect(wrapper.find('main').classes()).toContain('max-w-none')
    },
  )

  it('keeps the global header, footer, and constrained content area on home', async () => {
    const wrapper = await mountAt('/')
    await nextTick()

    expect(router.currentRoute.value.meta.standalone).not.toBe(true)
    expect(wrapper.find('[data-testid="app-header"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="app-footer"]').exists()).toBe(true)
    expect(wrapper.find('main').classes()).toContain('max-w-[1280px]')
    expect(wrapper.find('footer').text()).toContain('© 2026 CLICK-HUB. ALL RIGHTS RESERVED.')
    expect(wrapper.find('footer').text()).toContain('Privacy')
    expect(wrapper.find('footer').text()).toContain('Terms')
    expect(wrapper.find('footer').text()).not.toContain('문의하기')
  })
})
