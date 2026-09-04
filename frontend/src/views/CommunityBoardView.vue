<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'

import { createCommunityPost, getCommunityBoards, getCommunityPosts } from '@/api/community'
import CommunityPostList from '@/components/pages/community/CommunityPostList.vue'

const boards = ref([])
const activeBoard = ref('')
const posts = ref([])
const nextCursor = ref(null)
const query = ref('')
const isLoading = ref(true)
const errorMessage = ref('')
const showComposer = ref(false)
const isSaving = ref(false)
const form = reactive({ title: '', body: '' })

const visiblePosts = computed(() => {
  const needle = query.value.trim().toLowerCase()
  return posts.value.filter((post) =>
    `${post.title} ${post.authorName}`.toLowerCase().includes(needle),
  )
})

async function loadBoards() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    boards.value = await getCommunityBoards()
    activeBoard.value = boards.value[0]?.slug ?? ''
  } catch (error) {
    errorMessage.value = error.message
    isLoading.value = false
  }
}

async function loadPosts({ append = false } = {}) {
  if (!activeBoard.value) {
    isLoading.value = false
    return
  }
  isLoading.value = true
  errorMessage.value = ''
  try {
    const page = await getCommunityPosts(activeBoard.value, {
      cursor: append ? nextCursor.value : undefined,
    })
    posts.value = append ? [...posts.value, ...page.items] : page.items
    nextCursor.value = page.nextCursor
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function createPost() {
  if (!form.title.trim() || !form.body.trim() || isSaving.value) return
  isSaving.value = true
  errorMessage.value = ''
  try {
    await createCommunityPost(activeBoard.value, {
      title: form.title.trim(),
      body: form.body.trim(),
    })
    form.title = ''
    form.body = ''
    showComposer.value = false
    await loadPosts()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isSaving.value = false
  }
}

watch(activeBoard, () => void loadPosts())
onMounted(loadBoards)
</script>

<template>
  <section class="mx-auto w-full max-w-[1120px] py-4 sm:py-8" aria-labelledby="community-heading">
    <header
      class="mb-4 rounded-2xl border border-divider/15 bg-gradient-to-br from-surface-light-1 via-surface-light-1 to-primary-50 px-5 py-6 shadow-[0_10px_28px_rgba(15,14,71,0.04)] dark:border-blue-500/15 dark:from-surface-dark-1 dark:via-surface-dark-1 dark:to-primary-950 sm:flex sm:items-center sm:justify-between sm:px-7"
    >
      <div>
        <p class="mb-2 text-xs font-semibold text-primary-600 dark:text-blue-300">MAKER COMMONS</p>
        <h1
          id="community-heading"
          class="font-headline text-2xl font-extrabold tracking-tight text-heading-light dark:text-heading-dark"
        >
          커뮤니티 게시판
        </h1>
        <p class="mt-2 text-xs leading-5 text-body-light dark:text-body-dark">
          프로젝트 경험과 질문을 실제 사용자들과 나누는 공간입니다.
        </p>
      </div>
      <button
        type="button"
        class="mt-5 inline-flex shrink-0 items-center justify-center rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-primary-700 sm:mt-0"
        @click="showComposer = !showComposer"
      >
        새 글 작성하기
      </button>
    </header>

    <form
      v-if="showComposer"
      class="mb-4 space-y-3 rounded-xl border border-divider/20 bg-surface-light-1 p-5 dark:border-blue-500/15 dark:bg-surface-dark-1"
      @submit.prevent="createPost"
    >
      <input
        v-model="form.title"
        name="post-title"
        maxlength="200"
        required
        class="w-full rounded-lg border border-divider/20 bg-base-light px-3 py-2 text-sm text-heading-light outline-none focus:border-primary-600 dark:border-blue-500/15 dark:bg-base-dark dark:text-heading-dark"
        placeholder="제목"
      />
      <textarea
        v-model="form.body"
        name="post-body"
        maxlength="10000"
        required
        rows="5"
        class="w-full rounded-lg border border-divider/20 bg-base-light px-3 py-2 text-sm text-heading-light outline-none focus:border-primary-600 dark:border-blue-500/15 dark:bg-base-dark dark:text-heading-dark"
        placeholder="내용"
      />
      <button
        type="submit"
        :disabled="isSaving"
        class="rounded-lg bg-primary-600 px-5 py-2 text-sm font-bold text-white hover:bg-primary-700"
      >
        {{ isSaving ? '등록 중...' : '게시글 등록' }}
      </button>
    </form>

    <nav
      class="flex flex-wrap gap-2 rounded-xl border border-divider/15 bg-surface-light-1 p-2 dark:border-blue-500/15 dark:bg-surface-dark-1"
      aria-label="커뮤니티 게시판"
    >
      <button
        v-for="board in boards"
        :key="board.id"
        type="button"
        :aria-pressed="activeBoard === board.slug"
        class="rounded-md px-3 py-2 text-xs font-semibold"
        :class="
          activeBoard === board.slug
            ? 'bg-primary-600 text-white'
            : 'text-body-light dark:text-body-dark'
        "
        @click="activeBoard = board.slug"
      >
        {{ board.name }}
      </button>
    </nav>

    <input
      v-model="query"
      type="search"
      class="mt-3 w-full rounded-md border border-divider/20 bg-base-light px-3 py-2 text-xs text-heading-light outline-none placeholder:text-body-light focus:border-primary-600 dark:border-blue-500/15 dark:bg-base-dark dark:text-heading-dark"
      placeholder="게시글 제목, 작성자 검색"
    />

    <p v-if="isLoading" class="py-16 text-center text-sm text-body-light dark:text-body-dark">
      게시글을 불러오는 중입니다.
    </p>
    <div v-else-if="errorMessage" class="py-16 text-center">
      <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <button type="button" class="mt-4 font-semibold text-primary-600" @click="loadPosts()">
        다시 시도
      </button>
    </div>
    <template v-else>
      <div class="mt-5"><CommunityPostList :posts="visiblePosts" /></div>
      <button
        v-if="nextCursor"
        type="button"
        class="mx-auto mt-6 block rounded-lg border border-divider/20 px-5 py-2 text-sm font-semibold text-body-light dark:border-blue-500/15 dark:text-body-dark"
        @click="loadPosts({ append: true })"
      >
        더 보기
      </button>
    </template>
  </section>
</template>
