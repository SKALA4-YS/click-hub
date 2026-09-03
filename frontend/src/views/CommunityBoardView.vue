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
      class="rounded-2xl border border-divider/15 bg-white px-6 py-7 shadow-sm sm:flex sm:items-center sm:justify-between"
    >
      <div>
        <p class="text-xs font-semibold text-primary-600">MAKER COMMONS</p>
        <h1 id="community-heading" class="mt-2 font-headline text-2xl font-extrabold">
          커뮤니티 게시판
        </h1>
        <p class="mt-2 text-sm text-body-light">
          프로젝트 경험과 질문을 실제 사용자들과 나누는 공간입니다.
        </p>
      </div>
      <button
        type="button"
        class="mt-5 rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-semibold text-white sm:mt-0"
        @click="showComposer = !showComposer"
      >
        새 글 작성하기
      </button>
    </header>

    <form
      v-if="showComposer"
      class="mt-5 space-y-3 rounded-xl border border-divider/20 bg-white p-5"
      @submit.prevent="createPost"
    >
      <input
        v-model="form.title"
        name="post-title"
        maxlength="200"
        required
        class="w-full rounded-lg border border-divider/20 px-3 py-2"
        placeholder="제목"
      />
      <textarea
        v-model="form.body"
        name="post-body"
        maxlength="10000"
        required
        rows="5"
        class="w-full rounded-lg border border-divider/20 px-3 py-2"
        placeholder="내용"
      />
      <button
        type="submit"
        :disabled="isSaving"
        class="rounded-lg bg-primary-600 px-5 py-2 text-sm font-bold text-white"
      >
        {{ isSaving ? '등록 중...' : '게시글 등록' }}
      </button>
    </form>

    <nav class="mt-5 flex flex-wrap gap-2" aria-label="커뮤니티 게시판">
      <button
        v-for="board in boards"
        :key="board.id"
        type="button"
        :aria-pressed="activeBoard === board.slug"
        class="rounded-lg px-4 py-2 text-sm font-semibold"
        :class="
          activeBoard === board.slug
            ? 'bg-primary-600 text-white'
            : 'border border-divider/20 bg-white'
        "
        @click="activeBoard = board.slug"
      >
        {{ board.name }}
      </button>
    </nav>

    <input
      v-model="query"
      type="search"
      class="mt-5 w-full rounded-lg border border-divider/20 bg-white px-4 py-3 text-sm"
      placeholder="게시글 제목, 작성자 검색"
    />

    <p v-if="isLoading" class="py-16 text-center text-sm text-body-light">
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
        class="mx-auto mt-6 block rounded-lg border border-divider/20 px-5 py-2 text-sm font-semibold"
        @click="loadPosts({ append: true })"
      >
        더 보기
      </button>
    </template>
  </section>
</template>
