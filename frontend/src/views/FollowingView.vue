<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { getMySubscriptions, toggleCreatorSubscription } from '@/api/users'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const following = ref([])
const query = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const savingIds = ref(new Set())

const filteredFollowing = computed(() => {
  const needle = query.value.trim().toLowerCase()
  return following.value.filter((creator) => creator.displayName.toLowerCase().includes(needle))
})

async function loadFollowing() {
  if (!auth.isLoggedIn) return
  isLoading.value = true
  errorMessage.value = ''
  try {
    following.value = await getMySubscriptions()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function unfollow(creator) {
  if (savingIds.value.has(creator.id)) return
  savingIds.value.add(creator.id)
  try {
    const result = await toggleCreatorSubscription(creator.id)
    if (!result.subscribed)
      following.value = following.value.filter((item) => item.id !== creator.id)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    savingIds.value.delete(creator.id)
  }
}

onMounted(loadFollowing)
</script>

<template>
  <section v-if="!auth.isLoggedIn" class="mx-auto max-w-[1120px] py-28 text-center">
    <p class="text-sm text-body-light dark:text-body-dark">
      팔로잉 목록을 확인하려면 로그인이 필요합니다.
    </p>
    <RouterLink
      to="/login"
      class="mt-5 inline-flex rounded-lg bg-primary-600 px-6 py-3 text-sm font-bold text-white"
      >로그인하러 가기</RouterLink
    >
  </section>

  <section v-else class="mx-auto max-w-[1120px] pb-14" aria-labelledby="following-heading">
    <header class="flex flex-wrap items-end justify-between gap-5">
      <div>
        <h1
          id="following-heading"
          class="font-headline text-3xl font-extrabold text-heading-light dark:text-heading-dark"
        >
          내 팔로잉 관리
        </h1>
        <p class="mt-2 text-sm text-body-light dark:text-body-dark">
          구독한 메이커와 공개 프로젝트 수를 확인합니다.
        </p>
      </div>
      <strong
        class="rounded-xl border border-divider/20 bg-white px-5 py-3 text-heading-light dark:border-divider/30 dark:bg-surface-dark-1 dark:text-heading-dark"
        >팔로잉 메이커 {{ following.length }}명</strong
      >
    </header>

    <label
      class="mt-6 flex items-center rounded-xl border border-divider/20 bg-white px-4 py-3 text-sm text-heading-light dark:border-divider/30 dark:bg-surface-dark-1 dark:text-heading-dark"
    >
      <span class="mr-2">⌕</span>
      <input
        v-model="query"
        class="w-full bg-transparent outline-none"
        placeholder="메이커 이름 검색..."
      />
    </label>

    <p v-if="isLoading" class="py-16 text-center text-sm text-body-light dark:text-body-dark">
      팔로잉을 불러오는 중입니다.
    </p>
    <div v-else-if="errorMessage" class="py-16 text-center">
      <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <button type="button" class="mt-4 font-semibold text-primary-600" @click="loadFollowing">
        다시 시도
      </button>
    </div>
    <p
      v-else-if="filteredFollowing.length === 0"
      class="py-16 text-center text-sm text-body-light dark:text-body-dark"
    >
      구독 중인 메이커가 없습니다.
    </p>
    <div v-else class="mt-6 space-y-4">
      <article
        v-for="creator in filteredFollowing"
        :key="creator.id"
        data-testid="following-card"
        class="flex flex-wrap items-center gap-4 rounded-2xl border border-divider/20 bg-white p-5 dark:border-divider/30 dark:bg-surface-dark-1"
      >
        <img
          v-if="creator.avatarUrl"
          :src="creator.avatarUrl"
          :alt="creator.displayName"
          class="h-12 w-12 rounded-full object-cover"
        />
        <span
          v-else
          class="grid h-12 w-12 place-items-center rounded-full bg-primary-50 font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
          >{{ creator.displayName.slice(0, 1) }}</span
        >
        <div class="min-w-0 flex-1">
          <RouterLink
            :to="`/developers/${creator.id}`"
            class="font-headline text-lg font-extrabold text-heading-light hover:text-primary-600 dark:text-heading-dark"
            >{{ creator.displayName }}</RouterLink
          >
          <p class="mt-1 text-xs text-body-light dark:text-body-dark">
            구독자 {{ creator.subscriberCount }}명 · 공개 프로젝트 {{ creator.projectCount }}개
          </p>
        </div>
        <button
          type="button"
          :aria-label="`${creator.displayName} 팔로우 해제`"
          :disabled="savingIds.has(creator.id)"
          class="rounded-lg border border-divider/20 px-4 py-2 text-xs font-semibold"
          @click="unfollow(creator)"
        >
          {{ savingIds.has(creator.id) ? '처리 중...' : '✓ 팔로잉' }}
        </button>
      </article>
    </div>
  </section>
</template>
