import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { describe, expect, it } from 'vitest'

import DeveloperDetailView from '@/views/DeveloperDetailView.vue'

async function mountView() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/developers/:id', component: DeveloperDetailView },
      { path: '/projects/:id', component: { template: '<div>프로젝트</div>' } },
      { path: '/projects/new', component: { template: '<div>등록</div>' } },
    ],
  })
  await router.push('/developers/alex-kim')
  await router.isReady()
  return mount(DeveloperDetailView, { global: { plugins: [router] } })
}

describe('DeveloperDetailView', () => {
  it('shows the complete Figma maker dashboard', async () => {
    const wrapper = await mountView()

    expect(wrapper.get('h1').text()).toContain('김민준')
    expect(wrapper.text()).toContain('@alex_dev')
    expect(wrapper.text()).toContain('등록한 사이트')
    expect(wrapper.text()).toContain('웹 클릭수')
    expect(wrapper.text()).toContain('총 누적 조회수')
    expect(wrapper.findAll('a[href^="/projects/"]')).toHaveLength(4)
    expect(wrapper.text()).toContain('최근 받은 유저 피드백')
    expect(wrapper.text()).toContain('마이 퀵 메뉴')
  })

  it('switches profile tabs without leaving the page', async () => {
    const wrapper = await mountView()

    await wrapper.get('button[aria-label="활동 내역 / 통계 탭"]').trigger('click')
    expect(wrapper.get('button[aria-label="활동 내역 / 통계 탭"]').attributes('aria-selected')).toBe(
      'true',
    )
    expect(wrapper.text()).toContain('최근 메이커 활동')
  })
})
