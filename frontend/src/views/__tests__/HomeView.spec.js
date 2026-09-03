import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import HomeView from '@/views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'

function mountHome() {
  const pinia = createPinia()
  setActivePinia(pinia)

  return mount(HomeView, {
    global: {
      plugins: [pinia],
      stubs: {
        RouterLink: {
          props: ['to'],
          template: '<a :href="typeof to === \'string\' ? to : to.path"><slot /></a>',
        },
      },
    },
  })
}

describe('HomeView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('shows its feed in the Figma section order', () => {
    const wrapper = mountHome()

    expect(wrapper.findAll('h2').map((heading) => heading.text())).toEqual([
      'Top 100',
      '맞춤 추천',
      '내가 팔로잉한 개발자',
    ])
  })

  it('filters every feed section when a category tab is selected', async () => {
    const wrapper = mountHome()
    useAuthStore().mockLoginWithGoogle()

    await wrapper.get('button[aria-label="디자인 카테고리"]').trigger('click')

    expect(wrapper.text()).toContain('IconGenie Studio')
    expect(wrapper.text()).toContain('MoodPalette')
    expect(wrapper.text()).not.toContain('DevFlow Analytics')
    expect(wrapper.text()).not.toContain('GitPulse Activity')
  })

  it('links every visible project card to its project detail route', () => {
    const wrapper = mountHome()

    expect(wrapper.get('a[href="/projects/prj_301"]').text()).toContain('DevFlow Analytics')
  })

  it('replaces the signed-out following prompt with followed projects after login', async () => {
    const wrapper = mountHome()
    const auth = useAuthStore()

    expect(wrapper.text()).toContain('로그인하면 구독한 제작자가')

    auth.mockLoginWithGoogle()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('StudyMate Planner')
    expect(wrapper.text()).not.toContain('로그인하면 구독한 제작자가')
  })
})
