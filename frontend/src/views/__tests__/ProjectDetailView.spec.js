import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it } from 'vitest'

import ProjectDetailView from '@/views/ProjectDetailView.vue'
import { useAuthStore } from '@/stores/auth'

async function mountView({ loggedIn = false } = {}) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/projects/:id', component: ProjectDetailView },
      { path: '/login', component: { template: '<div>로그인</div>' } },
      { path: '/rankings', component: { template: '<div>프로젝트</div>' } },
      { path: '/', component: { template: '<div>홈</div>' } },
    ],
  })
  await router.push('/projects/prj_301')
  await router.isReady()
  if (loggedIn) useAuthStore().$patch({ user: { display_name: '김민준' } })

  return mount(ProjectDetailView, { global: { plugins: [router] } })
}

describe('ProjectDetailView', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('matches the Figma detail hierarchy from showcase to recommendations', async () => {
    const wrapper = await mountView()

    expect(wrapper.get('h1').text()).toContain('DevFlow Analytics')
    expect(wrapper.get('[data-testid="project-showcase"]')).toBeTruthy()
    expect(wrapper.text()).toContain('프로젝트 소개')
    expect(wrapper.text()).toContain('도움이 되는 피드백')
    expect(wrapper.text()).toContain('이런 사이트는 어때요?')
    expect(wrapper.text()).toContain('외부 클릭')
    expect(wrapper.text()).toContain('북마크')
  })

  it('keeps feedback and reactions as local UI interactions', async () => {
    const wrapper = await mountView({ loggedIn: true })

    await wrapper.get('button[aria-label="프로젝트 좋아요"]').trigger('click')
    expect(wrapper.get('button[aria-label="프로젝트 좋아요"]').attributes('aria-pressed')).toBe(
      'true',
    )

    await wrapper
      .get('textarea[name="feedback"]')
      .setValue('대시보드의 주간 비교 기능이 특히 좋았습니다.')
    await wrapper.get('button[name="submit-feedback"]').trigger('click')
    expect(wrapper.text()).toContain('대시보드의 주간 비교 기능이 특히 좋았습니다.')
  })
})
