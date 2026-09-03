import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import ProjectRegisterView from '@/views/ProjectRegisterView.vue'
import { useAuthStore } from '@/stores/auth'

const routerLinkStub = {
  props: ['to'],
  template: '<a :href="typeof to === \'string\' ? to : to.path"><slot /></a>',
}

function mountView() {
  return mount(ProjectRegisterView, {
    global: { stubs: { RouterLink: routerLinkStub } },
  })
}

describe('ProjectRegisterView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    useAuthStore().mockLoginWithGoogle()
  })

  it('presents the Figma registration flow and all major form sections', () => {
    const wrapper = mountView()

    expect(wrapper.get('h1').text()).toBe('새 프로젝트 등록하기')
    expect(wrapper.text()).toContain('01 배포 URL 및 AI 원클릭 분석')
    expect(wrapper.text()).toContain('02 프로젝트 기본 정보')
    expect(wrapper.text()).toContain('03 썸네일 & 대표 스크린샷')
    expect(wrapper.text()).toContain('04 코드 저장소 및 외부 링크')
    expect(wrapper.text()).toContain('05 기술 스택')
    expect(wrapper.get('button[type="submit"]').text()).toContain('프로젝트 등록 및 즉시 배포')
  })

  it('shows the Figma completion page after valid project details are submitted', async () => {
    const wrapper = mountView()

    await wrapper.get('[name="title"]').setValue('DevFlow Analytics')
    await wrapper
      .get('[name="description"]')
      .setValue('배포한 서비스를 한 곳에서 분석하는 대시보드입니다.')
    await wrapper.get('[name="siteUrl"]').setValue('https://devflow-analytics.io')
    await wrapper.get('[name="category"]').setValue('developer-tools')
    await wrapper.get('[name="agreed"]').setValue(true)
    await wrapper.get('form').trigger('submit')

    expect(wrapper.get('h1').text()).toBe('축하합니다! 프로젝트가 성공적으로 등록되었습니다!')
    expect(wrapper.text()).toContain('DevFlow Analytics')
    expect(wrapper.get('[data-testid="project-registration-success"]')).toBeTruthy()
    expect(wrapper.get('a[href="/projects/devflow-analytics"]').text()).toContain(
      '등록한 상세 페이지 보기',
    )
  })
})
