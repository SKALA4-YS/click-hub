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

  it('renders the Figma community header with its CTA and board totals', () => {
    const wrapper = mountCommunity()

    expect(wrapper.get('h1').text()).toBe('커뮤니티 게시판')
    expect(wrapper.get('a[href="/login"]').text()).toContain('새 글 작성하기')
    expect(wrapper.text()).toContain('전체글 1,248')
    expect(wrapper.text()).toContain('자유게시판 412')
    expect(wrapper.text()).toContain('정보공유 530')
    expect(wrapper.text()).toContain('IT / Q&A 306')
    expect(wrapper.text()).toContain('Click HUB 이용 가이드')
  })

  it('filters posts with the four Figma toolbar modes and board search', async () => {
    const wrapper = mountCommunity()

    expect(wrapper.get('[aria-label="게시글 정렬 및 필터"]').text()).toContain('최신순')
    expect(wrapper.get('[aria-label="게시글 정렬 및 필터"]').text()).toContain('인기순 (Trending)')
    expect(wrapper.get('[aria-label="게시글 정렬 및 필터"]').text()).toContain('댓글 많은순')
    expect(wrapper.get('[aria-label="게시글 정렬 및 필터"]').text()).toContain('# 해결된 질문만')

    await wrapper.get('input[placeholder="게시글 제목, 내용 검색"]').setValue('Supabase')

    expect(wrapper.text()).toContain('Supabase vs PostgreSQL')
    expect(wrapper.text()).not.toContain('Click HUB 이용 가이드')
  })

  it('shows Figma card metadata and its twelve-page pagination presentation', async () => {
    const wrapper = mountCommunity()

    expect(wrapper.text()).toContain('Click-Hub 운영팀')
    expect(wrapper.text()).toContain('조회')
    expect(wrapper.text()).toContain('좋아요')
    expect(wrapper.text()).toContain('댓글')
    expect(wrapper.get('[aria-label="게시글 페이지"]').text()).toContain('1')
    expect(wrapper.get('[aria-label="게시글 페이지"]').text()).toContain('5')
    expect(wrapper.get('[aria-label="게시글 페이지"]').text()).toContain('12')

    await wrapper.get('button[aria-label="2페이지"]').trigger('click')

    expect(wrapper.text()).toContain('프로젝트 소개글, 이렇게 써보세요')
    expect(wrapper.text()).not.toContain('Click HUB 이용 가이드')
  })

  it('shows the Figma sidebar blocks and routes signed-out writing through login', () => {
    const wrapper = mountCommunity()

    expect(wrapper.text()).toContain('메이커 커뮤니티 에티켓')
    expect(wrapper.text()).toContain('실시간 주간 핫포스트 TOP 5')
    expect(wrapper.text()).toContain('이번 주 우수 답변자')
    expect(wrapper.text()).toContain('인기 기술 & 주제 태그')
    expect(wrapper.text()).toContain('30일 동안 MVP 런칭하고 첫 수익 만들기')
    expect(wrapper.get('a[href="/login"]').text()).toContain('새 글 작성하기')
  })
})
