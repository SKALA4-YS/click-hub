import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import CommunityBoardView from '@/views/CommunityBoardView.vue'

function mountCommunity() {
  const pinia = createPinia()
  setActivePinia(pinia)

  return mount(CommunityBoardView, {
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

describe('CommunityBoardView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('shows the board tabs and post list from the static community fixture', () => {
    const wrapper = mountCommunity()

    expect(wrapper.get('h1').text()).toBe('커뮤니티')
    expect(wrapper.get('[aria-label="게시판 분류"]').text()).toContain('전체')
    expect(wrapper.get('[aria-label="게시판 분류"]').text()).toContain('공지')
    expect(wrapper.get('[aria-label="게시판 분류"]').text()).toContain('자유')
    expect(wrapper.get('[aria-label="게시판 분류"]').text()).toContain('질문답변')
    expect(wrapper.text()).toContain('Click HUB 이용 가이드')
  })

  it('filters posts when a board tab is selected', async () => {
    const wrapper = mountCommunity()

    await wrapper.get('button[aria-label="자유 게시판 보기"]').trigger('click')

    expect(wrapper.text()).toContain('첫 사이드 프로젝트를 공개했습니다')
    expect(wrapper.text()).not.toContain('Click HUB 이용 가이드')
  })

  it('changes the visible posts when a pagination control is selected', async () => {
    const wrapper = mountCommunity()

    expect(wrapper.text()).toContain('Click HUB 이용 가이드')
    await wrapper.get('button[aria-label="2페이지"]').trigger('click')

    expect(wrapper.text()).toContain('프로젝트 소개글, 이렇게 써보세요')
    expect(wrapper.text()).not.toContain('Click HUB 이용 가이드')
  })

  it('shows the sign-in treatment for writing a post', () => {
    const wrapper = mountCommunity()

    expect(wrapper.get('a[href="/login"]').text()).toContain('로그인하고 글쓰기')
  })
})
