<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { toSiteCardProject } from '@/api/adapters/projects'
import { getCreator, getMyProjects, toggleCreatorSubscription } from '@/api/users'
import SiteCard from '@/components/card/SiteCard.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const creator = ref(null)
const projects = ref([])
const isLoading = ref(true)
const errorMessage = ref('')
const isSaving = ref(false)

const creatorId = computed(() => route.params.id ?? auth.user?.id)
const isMine = computed(() => Boolean(auth.user?.id && auth.user.id === creatorId.value))

async function loadCreator() {
  if (!creatorId.value) {
    errorMessage.value = '로그인이 필요합니다.'
    isLoading.value = false
    return
  }
  isLoading.value = true
  errorMessage.value = ''
  try {
    creator.value = await getCreator(creatorId.value)
    const items = isMine.value ? await getMyProjects() : creator.value.projects
    projects.value = items.map(toSiteCardProject)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function toggleSubscription() {
  if (!auth.isLoggedIn) {
    await router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (isSaving.value) return
  isSaving.value = true
  try {
    const result = await toggleCreatorSubscription(creatorId.value)
    creator.value.subscribedByMe = result.subscribed
    creator.value.subscriberCount += result.subscribed ? 1 : -1
  } finally {
    isSaving.value = false
  }
}

onMounted(loadCreator)
watch(creatorId, loadCreator)
</script>

<template>
  <section class="mx-auto max-w-[1120px] pb-14" aria-labelledby="developer-detail-heading">
    <p v-if="isLoading" class="py-24 text-center text-sm text-body-light">
      프로필을 불러오는 중입니다.
    </p>
    <div v-else-if="errorMessage" class="py-24 text-center">
      <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <RouterLink to="/login" class="mt-4 inline-block font-semibold text-primary-600"
        >로그인하기</RouterLink
      >
    </div>
    <template v-else>
      <header class="rounded-2xl border border-divider/20 bg-white p-6 shadow-sm">
        <div class="flex flex-wrap items-center gap-5">
          <img
            v-if="creator.avatarUrl"
            :src="creator.avatarUrl"
            :alt="creator.displayName"
            class="h-20 w-20 rounded-full object-cover"
          />
          <span
            v-else
            class="grid h-20 w-20 place-items-center rounded-full bg-primary-50 text-xl font-bold text-primary-700"
          >
            {{ creator.displayName.slice(0, 1) }}
          </span>
          <div class="min-w-0 flex-1">
            <h1 id="developer-detail-heading" class="font-headline text-2xl font-extrabold">
              {{ creator.displayName }}
            </h1>
            <p class="mt-2 text-sm text-body-light">
              구독자 {{ creator.subscriberCount }}명 · 공개 프로젝트 {{ creator.projects.length }}개
            </p>
          </div>
          <RouterLink
            v-if="isMine"
            to="/projects/new"
            class="rounded-lg bg-primary-600 px-5 py-3 text-sm font-bold text-white"
          >
            새 프로젝트 등록
          </RouterLink>
          <button
            v-else
            type="button"
            :aria-pressed="creator.subscribedByMe"
            class="rounded-lg bg-primary-600 px-5 py-3 text-sm font-bold text-white"
            @click="toggleSubscription"
          >
            {{ creator.subscribedByMe ? '구독 중' : '구독하기' }}
          </button>
        </div>
      </header>

      <section class="mt-8">
        <h2 class="font-headline text-xl font-extrabold">
          {{ isMine ? '내 프로젝트' : '공개 프로젝트' }}
          <span class="text-sm text-primary-700">{{ projects.length }}건</span>
        </h2>
        <p v-if="projects.length === 0" class="py-16 text-center text-sm text-body-light">
          등록된 프로젝트가 없습니다.
        </p>
        <div v-else class="mt-5 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <SiteCard v-for="project in projects" :key="project.id" :project="project" />
        </div>
      </section>
    </template>
  </section>
</template>
