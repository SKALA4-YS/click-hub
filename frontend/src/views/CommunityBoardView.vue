<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import CommunityPostList from '@/components/pages/community/CommunityPostList.vue'
import { communityPosts } from '@/data/communityBoardFixture'

const activePage = ref(1)
const sort = ref('latest')
const searchQuery = ref('')
const resolvedOnly = ref(false)
const postsPerPage = 6

const filteredPosts = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  const posts = communityPosts.filter((post) => {
    const matchesQuery =
      !query || `${post.title} ${post.summary} ${post.tags.join(' ')}`.toLowerCase().includes(query)
    return matchesQuery && (!resolvedOnly.value || post.resolved)
  })
  if (sort.value === 'popular') return [...posts].sort((left, right) => right.likes - left.likes)
  if (sort.value === 'comments')
    return [...posts].sort((left, right) => right.comments - left.comments)
  return posts
})

const pageCount = computed(() =>
  searchQuery.value || resolvedOnly.value
    ? Math.max(1, Math.ceil(filteredPosts.value.length / postsPerPage))
    : 12,
)
const visiblePosts = computed(() =>
  filteredPosts.value.slice((activePage.value - 1) * postsPerPage, activePage.value * postsPerPage),
)

function selectPage(page) {
  activePage.value = page
}
function setSort(nextSort) {
  sort.value = nextSort
  activePage.value = 1
}
function resetPage() {
  activePage.value = 1
}
</script>

<template>
  <div class="mx-auto w-full max-w-[1120px] py-4 sm:py-8">
    <section
      class="mb-4 rounded-2xl border border-divider/15 bg-gradient-to-br from-surface-light-1 via-surface-light-1 to-primary-50 px-5 py-6 shadow-[0_10px_28px_rgba(15,14,71,0.04)] dark:border-blue-500/15 dark:from-surface-dark-1 dark:via-surface-dark-1 dark:to-primary-950 sm:flex sm:items-center sm:justify-between sm:px-7"
    >
      <div>
        <p class="mb-2 text-xs font-semibold text-primary-600 dark:text-blue-300">MAKER COMMONS</p>
        <h1
          class="font-headline text-2xl font-extrabold tracking-tight text-heading-light dark:text-heading-dark"
        >
          커뮤니티 게시판
        </h1>
        <p class="mt-2 text-xs leading-5 text-body-light dark:text-body-dark">
          사이드 프로젝트를 만들고 있는 사람들의 인사이트, 코드 리뷰와 협업, 솔직한 회고와 피드백
          공간
        </p>
      </div>
      <RouterLink
        to="/login"
        class="mt-5 inline-flex shrink-0 items-center justify-center rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-primary-700 sm:mt-0"
        >새 글 작성하기</RouterLink
      >
    </section>
    <div class="mb-4 flex flex-wrap gap-2" aria-label="게시판 통계">
      <span class="rounded-md bg-primary-600 px-3 py-1.5 text-xs font-semibold text-white"
        >전체글 1,248</span
      ><span
        class="rounded-md border border-divider/20 bg-surface-light-1 px-3 py-1.5 text-xs text-body-light dark:border-blue-500/15 dark:bg-surface-dark-1 dark:text-body-dark"
        >자유게시판 412</span
      ><span
        class="rounded-md border border-divider/20 bg-surface-light-1 px-3 py-1.5 text-xs text-body-light dark:border-blue-500/15 dark:bg-surface-dark-1 dark:text-body-dark"
        >정보공유 530</span
      ><span
        class="rounded-md border border-divider/20 bg-surface-light-1 px-3 py-1.5 text-xs text-body-light dark:border-blue-500/15 dark:bg-surface-dark-1 dark:text-body-dark"
        >IT / Q&amp;A 306</span
      >
    </div>
    <div class="grid gap-5 lg:grid-cols-[minmax(0,1fr)_290px]">
      <div>
        <div
          aria-label="게시글 정렬 및 필터"
          class="mb-3 flex flex-wrap items-center gap-2 rounded-xl border border-divider/15 bg-surface-light-1 p-2 dark:border-blue-500/15 dark:bg-surface-dark-1"
        >
          <button
            type="button"
            class="rounded-md px-3 py-2 text-xs font-semibold"
            :class="
              sort === 'latest'
                ? 'bg-primary-600 text-white'
                : 'text-body-light dark:text-body-dark'
            "
            @click="setSort('latest')"
          >
            최신순</button
          ><button
            type="button"
            class="rounded-md px-3 py-2 text-xs font-semibold"
            :class="
              sort === 'popular'
                ? 'bg-primary-600 text-white'
                : 'text-body-light dark:text-body-dark'
            "
            @click="setSort('popular')"
          >
            인기순 (Trending)</button
          ><button
            type="button"
            class="rounded-md px-3 py-2 text-xs font-semibold"
            :class="
              sort === 'comments'
                ? 'bg-primary-600 text-white'
                : 'text-body-light dark:text-body-dark'
            "
            @click="setSort('comments')"
          >
            댓글 많은순</button
          ><label class="flex items-center gap-1.5 px-2 text-xs text-body-light dark:text-body-dark"
            ><input
              v-model="resolvedOnly"
              type="checkbox"
              class="accent-primary-600"
              @change="resetPage"
            /># 해결된 질문만</label
          ><input
            v-model="searchQuery"
            type="search"
            placeholder="게시글 제목, 내용 검색"
            class="min-w-[180px] flex-1 rounded-md border border-divider/20 bg-base-light px-3 py-2 text-xs text-heading-light outline-none placeholder:text-body-light focus:border-primary-600 dark:border-blue-500/15 dark:bg-base-dark dark:text-heading-dark"
            @input="resetPage"
          />
        </div>
        <CommunityPostList :posts="visiblePosts" />
        <nav aria-label="게시글 페이지" class="mt-7 flex items-center justify-center gap-1">
          <button
            type="button"
            aria-label="이전 페이지"
            class="h-8 min-w-8 rounded-md text-xs text-body-light disabled:opacity-40 dark:text-body-dark"
            :disabled="activePage === 1"
            @click="selectPage(activePage - 1)"
          >
            이전</button
          ><template v-for="page in [1, 2, 3, 4, 5]" :key="page"
            ><button
              type="button"
              :aria-label="`${page}페이지`"
              :aria-current="activePage === page ? 'page' : undefined"
              class="h-8 min-w-8 rounded-md text-xs font-semibold"
              :class="
                activePage === page
                  ? 'bg-primary-600 text-white'
                  : 'text-body-light dark:text-body-dark'
              "
              @click="selectPage(page)"
            >
              {{ page }}
            </button></template
          ><span class="px-1 text-xs text-body-light dark:text-body-dark">…</span
          ><button
            type="button"
            aria-label="12페이지"
            class="h-8 min-w-8 rounded-md text-xs font-semibold text-body-light dark:text-body-dark"
            @click="selectPage(12)"
          >
            12</button
          ><button
            type="button"
            aria-label="다음 페이지"
            class="h-8 min-w-8 rounded-md text-xs text-body-light disabled:opacity-40 dark:text-body-dark"
            :disabled="activePage === pageCount"
            @click="selectPage(activePage + 1)"
          >
            다음
          </button>
        </nav>
      </div>
      <aside class="space-y-4">
        <section
          class="rounded-2xl border border-divider/15 bg-surface-light-1 p-5 dark:border-blue-500/15 dark:bg-surface-dark-1"
        >
          <h2 class="font-headline text-sm font-bold text-heading-light dark:text-heading-dark">
            메이커 커뮤니티 에티켓
          </h2>
          <ul class="mt-3 space-y-2 text-xs leading-5 text-body-light dark:text-body-dark">
            <li>서로 존중과 격려를 기본으로 해주세요.</li>
            <li>프로젝트 홍보는 맥락과 함께 공유해요.</li>
            <li>도움이 된 답변에는 감사 인사를 남겨요.</li>
          </ul>
        </section>
        <section
          class="rounded-2xl border border-divider/15 bg-surface-light-1 p-5 dark:border-blue-500/15 dark:bg-surface-dark-1"
        >
          <h2 class="font-headline text-sm font-bold text-heading-light dark:text-heading-dark">
            실시간 주간 핫포스트 TOP 5
          </h2>
          <ol class="mt-4 space-y-3">
            <li
              v-for="(post, index) in communityPosts.slice(2, 7)"
              :key="post.id"
              class="flex gap-2 text-xs"
            >
              <span class="font-bold text-primary-600 dark:text-blue-300">{{ index + 1 }}</span
              ><span class="min-w-0 truncate text-body-light dark:text-body-dark">{{
                post.title
              }}</span>
            </li>
          </ol>
        </section>
        <section
          class="rounded-2xl border border-divider/15 bg-surface-light-1 p-5 dark:border-blue-500/15 dark:bg-surface-dark-1"
        >
          <div class="flex items-center justify-between">
            <h2 class="font-headline text-sm font-bold text-heading-light dark:text-heading-dark">
              이번 주 우수 답변자
            </h2>
            <span
              class="rounded-full bg-amber-50 px-2 py-1 text-[10px] font-semibold text-amber-700"
              >Top Helpers</span
            >
          </div>
          <ol class="mt-4 space-y-3 text-xs text-body-light dark:text-body-dark">
            <li>Sarah Park <span class="float-right">24개 채택</span></li>
            <li>정우진 (@woojin_ai) <span class="float-right">18개 채택</span></li>
            <li>송하윤 (@haeun_fe) <span class="float-right">15개 채택</span></li>
          </ol>
        </section>
        <section
          class="rounded-2xl border border-divider/15 bg-surface-light-1 p-5 dark:border-blue-500/15 dark:bg-surface-dark-1"
        >
          <h2 class="font-headline text-sm font-bold text-heading-light dark:text-heading-dark">
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
                'SaaS',
                'AI 프로젝트',
                'MVP',
              ]"
              :key="tag"
              class="rounded-md bg-neutral-100 px-2 py-1 text-xs text-body-light dark:bg-surface-dark-2 dark:text-body-dark"
              >#{{ tag }}</span
            >
          </div>
        </section>
        <section class="rounded-2xl bg-gradient-to-br from-secondary to-primary-600 p-5 text-white">
          <p class="text-xs font-semibold text-blue-100">9월 메이커 챌린지</p>
          <h2 class="mt-3 font-headline text-lg font-bold">
            30일 동안 MVP 런칭하고 첫 수익 만들기
          </h2>
          <RouterLink
            to="/login"
            class="mt-5 inline-flex rounded-lg bg-white px-3 py-2 text-xs font-semibold text-primary-700"
            >챌린지 참가하기</RouterLink
          >
        </section>
      </aside>
    </div>
  </div>
</template>
