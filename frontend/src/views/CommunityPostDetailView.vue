<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import {
  createCommunityComment,
  deleteCommunityPost,
  getCommunityComments,
  getCommunityPost,
  updateCommunityPost,
} from '@/api/community'

const route = useRoute()
const router = useRouter()
const post = ref(null)
const comments = ref([])
const commentBody = ref('')
const editForm = reactive({ title: '', body: '' })
const isEditing = ref(false)
const isLoading = ref(true)
const isSaving = ref(false)
const errorMessage = ref('')

async function load() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    ;[post.value, comments.value] = await Promise.all([
      getCommunityPost(route.params.id),
      getCommunityComments(route.params.id),
    ])
    editForm.title = post.value.title
    editForm.body = post.value.body
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function addComment() {
  if (!commentBody.value.trim() || isSaving.value) return
  isSaving.value = true
  try {
    const comment = await createCommunityComment(route.params.id, {
      body: commentBody.value.trim(),
      parentId: null,
    })
    comments.value.push(comment)
    commentBody.value = ''
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isSaving.value = false
  }
}

async function savePost() {
  if (!editForm.title.trim() || !editForm.body.trim()) return
  await updateCommunityPost(route.params.id, {
    title: editForm.title.trim(),
    body: editForm.body.trim(),
  })
  post.value = { ...post.value, title: editForm.title.trim(), body: editForm.body.trim() }
  isEditing.value = false
}

async function removePost() {
  await deleteCommunityPost(route.params.id)
  await router.replace('/community')
}

onMounted(load)
</script>

<template>
  <section class="mx-auto max-w-[900px] pb-14">
    <p v-if="isLoading" class="py-20 text-center text-sm text-body-light">
      게시글을 불러오는 중입니다.
    </p>
    <p v-else-if="errorMessage" role="alert" class="py-20 text-center text-sm text-danger">
      {{ errorMessage }}
    </p>
    <template v-else>
      <article class="rounded-2xl border border-divider/20 bg-white p-6">
        <template v-if="isEditing">
          <input
            v-model="editForm.title"
            aria-label="게시글 제목 수정"
            class="w-full rounded-lg border border-divider/20 px-3 py-2"
          />
          <textarea
            v-model="editForm.body"
            aria-label="게시글 내용 수정"
            rows="8"
            class="mt-3 w-full rounded-lg border border-divider/20 px-3 py-2"
          />
          <button
            type="button"
            class="mt-3 rounded-lg bg-primary-600 px-4 py-2 text-sm text-white"
            @click="savePost"
          >
            수정 저장
          </button>
        </template>
        <template v-else>
          <h1 class="font-headline text-2xl font-extrabold">{{ post.title }}</h1>
          <p class="mt-2 text-xs text-body-light">
            {{ post.authorName }} · 조회 {{ post.viewCount }}
          </p>
          <p class="mt-6 whitespace-pre-wrap text-sm leading-7">{{ post.body }}</p>
          <div v-if="post.mine" class="mt-5 flex gap-2">
            <button
              type="button"
              class="rounded-lg border border-divider/20 px-4 py-2 text-sm"
              @click="isEditing = true"
            >
              수정
            </button>
            <button
              type="button"
              class="rounded-lg border border-danger px-4 py-2 text-sm text-danger"
              @click="removePost"
            >
              삭제
            </button>
          </div>
        </template>
      </article>

      <section class="mt-6 rounded-2xl border border-divider/20 bg-white p-6">
        <h2 class="font-headline text-lg font-bold">댓글 {{ comments.length }}개</h2>
        <form class="mt-4 flex gap-2" @submit.prevent="addComment">
          <input
            v-model="commentBody"
            aria-label="댓글 내용"
            maxlength="3000"
            required
            class="min-w-0 flex-1 rounded-lg border border-divider/20 px-3 py-2"
            placeholder="댓글을 입력하세요"
          />
          <button
            type="submit"
            :disabled="isSaving"
            class="rounded-lg bg-primary-600 px-4 py-2 text-sm font-bold text-white"
          >
            등록
          </button>
        </form>
        <ul class="mt-5 divide-y divide-divider/15">
          <li v-for="comment in comments" :key="comment.id" class="py-4 text-sm">
            <strong>{{ comment.authorName }}</strong>
            <p class="mt-2">{{ comment.body }}</p>
          </li>
        </ul>
      </section>
    </template>
  </section>
</template>
