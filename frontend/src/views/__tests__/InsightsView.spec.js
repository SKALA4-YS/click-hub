import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import InsightsView from '@/views/InsightsView.vue'

const getWeeklyInsight = vi.hoisted(() => vi.fn())
vi.mock('@/api/insights', () => ({ getWeeklyInsight }))

const weeklyInsight = {
  weekStart: '2026-09-01',
  headline: '실제 주간 인사이트',
  trends: [{ topic: 'Vue', direction: 'UP', changeRate: 12.5 }],
  watchlist: ['pgvector', 'Spring Boot'],
  modelName: 'MVP seed',
  generatedAt: '2026-09-04T00:00:00Z',
}

describe('InsightsView', () => {
  beforeEach(() => {
    getWeeklyInsight.mockReset().mockResolvedValue(weeklyInsight)
  })

  it('renders the weekly insight returned by the backend', async () => {
    const wrapper = mount(InsightsView)
    await flushPromises()

    expect(wrapper.get('h1').text()).toBe('실제 주간 인사이트')
    expect(wrapper.text()).toContain('Vue')
    expect(wrapper.text()).toContain('+12.5%')
    expect(wrapper.text()).toContain('pgvector')
    expect(wrapper.text()).toContain('Spring Boot')
    expect(getWeeklyInsight).toHaveBeenCalledOnce()
  })

  it('shows a retryable API error and retries the request', async () => {
    getWeeklyInsight
      .mockRejectedValueOnce(new Error('인사이트 연결 실패'))
      .mockResolvedValueOnce(weeklyInsight)
    const wrapper = mount(InsightsView)
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toBe('인사이트 연결 실패')
    await wrapper.get('button').trigger('click')
    await flushPromises()

    expect(getWeeklyInsight).toHaveBeenCalledTimes(2)
    expect(wrapper.text()).toContain('실제 주간 인사이트')
  })
})
