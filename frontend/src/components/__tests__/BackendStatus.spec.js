import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import BackendStatus from '../BackendStatus.vue'
import { waitForBackend } from '@/services/api'

vi.mock('@/services/api', () => ({
  waitForBackend: vi.fn(),
}))

describe('BackendStatus', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows the connected backend service', async () => {
    waitForBackend.mockResolvedValue({ status: 'ok', service: 'click-hub-backend' })

    const wrapper = mount(BackendStatus)
    await flushPromises()

    expect(wrapper.text()).toContain('Connected to click-hub-backend')
  })

  it('shows an error and retries manually', async () => {
    waitForBackend
      .mockRejectedValueOnce(new Error('Backend unavailable'))
      .mockResolvedValueOnce({ status: 'ok', service: 'click-hub-backend' })

    const wrapper = mount(BackendStatus)
    await flushPromises()
    expect(wrapper.text()).toContain('Could not connect')

    await wrapper.get('button').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Connected to click-hub-backend')
    expect(waitForBackend).toHaveBeenCalledTimes(2)
  })
})
