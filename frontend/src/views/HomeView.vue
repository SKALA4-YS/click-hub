<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import CategoryTabs from '@/components/layout/CategoryTabs.vue'
import SiteSection from '@/components/section/SiteSection.vue'
import SiteCard from '@/components/card/SiteCard.vue'
import { toSiteCardProject, mergeRankingsWithProjects } from '@/api/adapters/projects'
import { getFeed } from '@/api/feed'
import { getProjectRankings } from '@/api/rankings'
import { getCreator, getMySubscriptions } from '@/api/users'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const selectedCategory = ref(null)
const feedProjects = ref([])
const rankedProjects = ref([])
const followedProjects = ref([])
const isLoading = ref(true)
const errorMessage = ref('')

function filterByCategory(items) {
  if (!selectedCategory.value) return items
  return items.filter((item) => item.category_slug === selectedCategory.value)
}

const topRankedProjects = computed(() => filterByCategory(rankedProjects.value).slice(0, 6))
const recommendedProjects = computed(() => filterByCategory(feedProjects.value).slice(0, 6))
const followingProjects = computed(() => filterByCategory(followedProjects.value).slice(0, 6))

async function loadPublicFeed() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const [feed, rankings] = await Promise.all([getFeed(), getProjectRankings()])
    feedProjects.value = feed.items.map(toSiteCardProject)
    rankedProjects.value = mergeRankingsWithProjects(rankings, feedProjects.value)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function loadFollowedProjects() {
  if (!auth.isLoggedIn) {
    followedProjects.value = []
    return
  }
  try {
    const subscriptions = await getMySubscriptions()
    const creators = await Promise.all(subscriptions.map(({ id }) => getCreator(id)))
    followedProjects.value = creators.flatMap((creator) => creator.projects.map(toSiteCardProject))
  } catch {
    followedProjects.value = []
  }
}

onMounted(loadPublicFeed)
watch(() => auth.isLoggedIn, loadFollowedProjects, { immediate: true })
</script>

<template>
  <div class="flex flex-col gap-10">
    <CategoryTabs v-model="selectedCategory" />

    <p v-if="isLoading" class="py-12 text-center text-sm text-body-light">피드를 불러오는 중...</p>
    <section v-else-if="errorMessage" class="rounded-xl border border-danger/20 p-8 text-center">
      <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <button
        class="mt-4 text-sm font-semibold text-primary-600"
        type="button"
        @click="loadPublicFeed"
      >
        다시 시도
      </button>
    </section>

    <SiteSection v-else title="Top 100" badge="HOT" moreTo="/rankings" :items="topRankedProjects">
      <template #default="{ item, index }">
        <SiteCard :project="item" :rank="index + 1" />
      </template>
    </SiteSection>

    <SiteSection
      v-if="!isLoading && !errorMessage"
      title="최신 프로젝트"
      moreTo="/rankings?sort=latest"
      :items="recommendedProjects"
    >
      <template #default="{ item }">
        <SiteCard :project="item" />
      </template>
    </SiteSection>

    <SiteSection v-if="auth.isLoggedIn" title="내가 팔로잉한 개발자" :items="followingProjects">
      <template #default="{ item }">
        <SiteCard :project="item" />
      </template>
    </SiteSection>

    <!-- 로그인 전엔 섹션 자체를 숨기지 않고, 로그인하면 뭘 볼 수 있는지 안내한다 -->
    <section v-else class="flex flex-col gap-4">
      <h2 class="font-headline text-xl font-bold text-heading-light dark:text-heading-dark">
        내가 팔로잉한 개발자
      </h2>
      <div
        class="flex flex-col items-center gap-3 rounded-xl border border-divider/20 py-12 text-center dark:border-divider/25"
      >
        <p class="text-sm text-body-light dark:text-body-dark">
          로그인하면 구독한 제작자가 새로 올린 프로젝트를 여기서 바로 볼 수 있어요.
        </p>
        <RouterLink
          to="/login"
          class="rounded-full bg-primary-600 px-4 py-2 text-sm font-semibold text-white hover:bg-primary-700"
        >
          로그인하기
        </RouterLink>
      </div>
    </section>
  </div>

  <!-- 프로젝트 등록 플로팅 버튼 — /projects/new 라우트는 아직 없어 자리만 잡아둔다 -->
  <RouterLink
    to="/projects/new"
    class="fixed bottom-6 right-6 flex h-14 w-14 items-center justify-center rounded-full bg-secondary text-white shadow-lg transition-transform hover:scale-105"
    title="프로젝트 등록하기"
  >
    <svg viewBox="0 0 20 20" fill="currentColor" class="h-6 w-6">
      <path
        d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"
      />
    </svg>
  </RouterLink>
</template>
