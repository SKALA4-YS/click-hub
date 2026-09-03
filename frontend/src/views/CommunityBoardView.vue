<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import CommunityPostList from '@/components/pages/community/CommunityPostList.vue'
import { communityBoards, communityPosts } from '@/data/communityBoardFixture'

const activeBoard = ref('all')
const activePage = ref(1)
const sort = ref('latest')
const postsPerPage = 6

const filteredPosts = computed(() => {
  const posts =
    activeBoard.value === 'all'
      ? communityPosts
      : communityPosts.filter((post) => post.board === activeBoard.value)

  return sort.value === 'popular'
    ? [...posts].sort((left, right) => right.views - left.views)
    : posts
})

const pageCount = computed(() => Math.max(1, Math.ceil(filteredPosts.value.length / postsPerPage)))
const visiblePosts = computed(() => {
  const start = (activePage.value - 1) * postsPerPage
  return filteredPosts.value.slice(start, start + postsPerPage)
})

function selectBoard(boardId) {
  activeBoard.value = boardId
  activePage.value = 1
}

function selectPage(page) {
  activePage.value = page
}
</script>

<template>
  <div class="mx-auto w-full max-w-[1120px] py-4 sm:py-8">
    <section
      class="mb-6 rounded-2xl border border-divider/15 bg-gradient-to-br from-surface-light-1 via-surface-light-1 to-primary-50 px-5 py-7 shadow-[0_10px_28px_rgba(15,14,71,0.04)] dark:border-blue-500/15 dark:from-surface-dark-1 dark:via-surface-dark-1 dark:to-primary-950 sm:flex sm:items-end sm:justify-between sm:px-8"
    >
      <div>
        <p class="mb-2 text-sm font-semibold text-primary-600 dark:text-blue-300">COMMUNITY</p>
        <h1
          class="font-headline text-3xl font-extrabold tracking-tight text-heading-light dark:text-heading-dark"
        >
          커뮤니티
        </h1>
        <p class="mt-2 text-sm text-body-light dark:text-body-dark">
          프로젝트를 만들고 있는 사람들과 경험과 질문을 나눠보세요.
        </p>
      </div>
      <RouterLink
        to="/login"
        class="mt-5 inline-flex shrink-0 items-center justify-center rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-primary-700 sm:mt-0"
        >로그인하고 글쓰기</RouterLink
      >
    </section>

    <div class="mb-5 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div
        aria-label="게시판 분류"
        class="flex gap-1 overflow-x-auto border-b border-divider/20 dark:border-blue-500/15"
      >
        <button
          v-for="board in communityBoards"
          :key="board.id"
          type="button"
          :aria-label="`${board.label} 게시판 보기`"
          :aria-pressed="activeBoard === board.id"
          class="shrink-0 border-b-2 px-3 py-3 text-sm font-semibold transition-colors"
          :class="
            activeBoard === board.id
              ? 'border-primary-600 text-primary-600 dark:border-blue-300 dark:text-blue-300'
              : 'border-transparent text-body-light hover:text-heading-light dark:text-body-dark dark:hover:text-heading-dark'
          "
          @click="selectBoard(board.id)"
        >
          {{ board.label }}
        </button>
      </div>
      <label class="flex shrink-0 items-center gap-2 text-sm text-body-light dark:text-body-dark">
        <span class="sr-only">게시글 정렬</span>
        <select
          v-model="sort"
          class="rounded-lg border border-divider/30 bg-surface-light-1 px-3 py-2 text-sm text-heading-light outline-none focus:border-primary-600 dark:border-blue-500/20 dark:bg-surface-dark-1 dark:text-heading-dark"
        >
          <option value="latest">최신순</option>
          <option value="popular">인기순</option>
        </select>
      </label>
    </div>

    <div class="grid gap-6 lg:grid-cols-[minmax(0,1fr)_290px]">
      <CommunityPostList :posts="visiblePosts" />
      <aside class="space-y-5">
        <section
          class="rounded-2xl border border-divider/15 bg-surface-light-1 p-5 dark:border-blue-500/15 dark:bg-surface-dark-1"
        >
          <div class="mb-4 flex items-center justify-between">
            <h2 class="font-headline text-base font-bold text-heading-light dark:text-heading-dark">
              인기 게시글
            </h2>
            <span class="text-xs text-primary-600 dark:text-blue-300">이번 주</span>
          </div>
          <ol class="space-y-3">
            <li
              v-for="(post, index) in communityPosts.slice(2, 7)"
              :key="post.id"
              class="flex gap-3 text-sm"
            >
              <span class="font-bold text-primary-600 dark:text-blue-300">{{ index + 1 }}</span
              ><button
                type="button"
                class="min-w-0 truncate text-left text-body-light hover:text-primary-600 dark:text-body-dark"
              >
                {{ post.title }}
              </button>
            </li>
          </ol>
        </section>
        <section
          class="rounded-2xl border border-divider/15 bg-surface-light-1 p-5 dark:border-blue-500/15 dark:bg-surface-dark-1"
        >
          <h2 class="font-headline text-base font-bold text-heading-light dark:text-heading-dark">
            인기 기술 &amp; 주제 태그
          </h2>
          <div class="mt-4 flex flex-wrap gap-2">
            <span
              v-for="tag in [
                'Next.js',
                'Vue.js',
                'TailwindCSS',
                'Spring Boot',
                'Supabase',
                '인디해커',
                'MVP',
              ]"
              :key="tag"
              class="rounded-md bg-neutral-100 px-2.5 py-1.5 text-xs text-body-light dark:bg-surface-dark-2 dark:text-body-dark"
              >#{{ tag }}</span
            >
          </div>
        </section>
        <section class="rounded-2xl bg-gradient-to-br from-secondary to-primary-600 p-5 text-white">
          <p class="text-xs font-semibold text-blue-100">9월 메이커 챌린지</p>
          <h2 class="mt-3 font-headline text-xl font-bold">
            30일 동안 MVP 런칭하고 첫 수익 만들기
          </h2>
          <RouterLink
            to="/login"
            class="mt-5 inline-flex rounded-lg bg-white px-3 py-2 text-sm font-semibold text-primary-700"
            >챌린지 참가하기</RouterLink
          >
        </section>
      </aside>
    </div>

    <nav
      v-if="pageCount > 1"
      aria-label="게시글 페이지"
      class="mt-7 flex items-center justify-center gap-1"
    >
      <button
        type="button"
        class="h-9 min-w-9 rounded-lg px-2 text-sm text-body-light disabled:opacity-35 dark:text-body-dark"
        :disabled="activePage === 1"
        aria-label="이전 페이지"
        @click="selectPage(activePage - 1)"
      >
        이전
      </button>
      <button
        v-for="page in pageCount"
        :key="page"
        type="button"
        :aria-label="`${page}페이지`"
        :aria-current="activePage === page ? 'page' : undefined"
        class="h-9 min-w-9 rounded-lg px-2 text-sm font-semibold"
        :class="
          activePage === page
            ? 'bg-primary-600 text-white'
            : 'text-body-light hover:bg-primary-50 dark:text-body-dark dark:hover:bg-surface-dark-2'
        "
        @click="selectPage(page)"
      >
        {{ page }}
      </button>
      <button
        type="button"
        class="h-9 min-w-9 rounded-lg px-2 text-sm text-body-light disabled:opacity-35 dark:text-body-dark"
        :disabled="activePage === pageCount"
        aria-label="다음 페이지"
        @click="selectPage(activePage + 1)"
      >
        다음
      </button>
    </nav>

    <aside
      class="mt-10 rounded-2xl border border-primary-100 bg-primary-50 px-5 py-5 dark:border-blue-500/20 dark:bg-surface-dark-1 sm:flex sm:items-center sm:justify-between"
    >
      <div>
        <h2 class="font-headline text-base font-bold text-heading-light dark:text-heading-dark">
          커뮤니티에 참여해 보세요
        </h2>
        <p class="mt-1 text-sm text-body-light dark:text-body-dark">
          로그인하면 글을 쓰고, 댓글로 다른 메이커와 대화할 수 있어요.
        </p>
      </div>
      <RouterLink
        to="/login"
        class="mt-4 inline-flex text-sm font-semibold text-primary-600 hover:underline dark:text-blue-300 sm:mt-0"
        >로그인하기</RouterLink
      >
    </aside>
  </div>
</template>
