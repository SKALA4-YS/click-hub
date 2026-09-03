<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { toSiteCardProject } from '@/api/adapters/projects'
import { toggleProjectFavorite } from '@/api/projects'
import { getMyFavorites } from '@/api/users'
import SiteCard from '@/components/card/SiteCard.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const favorites = ref([])
const query = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const removingIds = ref(new Set())

const visibleFavorites = computed(() => {
  const needle = query.value.trim().toLowerCase()
  return favorites.value.filter((project) =>
    `${project.title} ${project.category} ${project.owner_name}`.toLowerCase().includes(needle),
  )
})

async function loadFavorites() {
  if (!auth.isLoggedIn) return
  isLoading.value = true
  errorMessage.value = ''
  try {
    favorites.value = (await getMyFavorites()).map(toSiteCardProject)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function remove(project) {
  if (removingIds.value.has(project.id)) return
  removingIds.value.add(project.id)
  try {
    const result = await toggleProjectFavorite(project.id)
    if (!result.favorited) {
      favorites.value = favorites.value.filter((item) => item.id !== project.id)
    }
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    removingIds.value.delete(project.id)
  }
}

onMounted(loadFavorites)
</script>

<template>
  <section v-if="!auth.isLoggedIn" class="mx-auto max-w-[1120px] py-28 text-center">
    <p class="text-sm text-body-light">즐겨찾기 보관함을 확인하려면 로그인이 필요합니다.</p>
    <RouterLink
      to="/login"
      class="mt-5 inline-flex rounded-lg bg-primary-600 px-6 py-3 text-sm font-bold text-white"
      >로그인하러 가기</RouterLink
    >
  </section>

  <section v-else class="mx-auto max-w-[1120px] pb-14" aria-labelledby="favorites-heading">
    <header class="rounded-2xl border border-divider/20 bg-white p-6 shadow-sm">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 id="favorites-heading" class="font-headline text-2xl font-extrabold">
            즐겨찾기 보관함
          </h1>
          <p class="mt-2 text-sm text-body-light">
            다시 보고 싶은 프로젝트를 서버에 저장하고 관리합니다.
          </p>
        </div>
        <span class="rounded-full bg-primary-50 px-3 py-1 text-xs font-bold text-primary-700"
          >★ {{ favorites.length }}개 저장됨</span
        >
      </div>
    </header>

    <label
      class="mt-6 flex items-center rounded-xl border border-divider/20 bg-white px-4 py-3 text-sm"
    >
      <span class="mr-2">⌕</span>
      <input
        v-model="query"
        name="favorite-search"
        class="w-full outline-none"
        placeholder="저장한 프로젝트 검색..."
      />
    </label>

    <p v-if="isLoading" class="py-16 text-center text-sm text-body-light">
      즐겨찾기를 불러오는 중입니다.
    </p>
    <div v-else-if="errorMessage" class="py-16 text-center">
      <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <button type="button" class="mt-4 font-semibold text-primary-600" @click="loadFavorites">
        다시 시도
      </button>
    </div>
    <p v-else-if="visibleFavorites.length === 0" class="py-16 text-center text-sm text-body-light">
      저장된 프로젝트가 없습니다.
    </p>
    <div v-else class="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
      <article
        v-for="project in visibleFavorites"
        :key="project.id"
        data-testid="favorite-card"
        class="rounded-xl border border-divider/20 bg-white p-4"
      >
        <SiteCard :project="project" />
        <button
          type="button"
          :aria-label="`${project.title} 즐겨찾기 해제`"
          :disabled="removingIds.has(project.id)"
          class="mt-3 w-full rounded-lg border border-divider/20 py-2 text-xs font-semibold text-primary-700"
          @click="remove(project)"
        >
          {{ removingIds.has(project.id) ? '해제 중...' : '즐겨찾기 해제' }}
        </button>
      </article>
    </div>
  </section>
</template>
