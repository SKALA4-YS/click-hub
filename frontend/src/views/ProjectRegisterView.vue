<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { getCategories, getTechnologies } from '@/api/catalog'
import { createProject, submitProject as submitProjectForReview } from '@/api/projects'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const categories = ref([])
const technologies = ref([])
const createdProjectId = ref('')
const submittedStatus = ref('')
const isSubmitting = ref(false)
const requestError = ref('')
const tagDraft = ref('')
const form = reactive({
  title: '',
  description: '',
  siteUrl: '',
  repositoryUrl: '',
  pricing: 'UNKNOWN',
  categorySlug: '',
  tags: [],
  technologySlugs: [],
  agreed: false,
})

const technologyGroups = computed(() =>
  technologies.value.reduce((groups, item) => {
    ;(groups[item.defaultGroup] ??= []).push(item)
    return groups
  }, {}),
)

function addTag() {
  const value = tagDraft.value.trim()
  if (value && !form.tags.includes(value)) form.tags.push(value)
  tagDraft.value = ''
}

function removeTag(tag) {
  form.tags = form.tags.filter((item) => item !== tag)
}

function toggleTechnology(slug) {
  form.technologySlugs = form.technologySlugs.includes(slug)
    ? form.technologySlugs.filter((item) => item !== slug)
    : [...form.technologySlugs, slug]
}

async function submitProject() {
  if (!form.agreed || isSubmitting.value) return
  isSubmitting.value = true
  requestError.value = ''
  try {
    const created = await createProject({
      title: form.title.trim(),
      description: form.description.trim(),
      siteUrl: form.siteUrl.trim(),
      repositoryUrl: form.repositoryUrl.trim() || null,
      pricing: form.pricing,
      tags: form.tags,
      thumbnailUrl: null,
      screenshots: [],
      techStacks: form.technologySlugs.map((technologySlug) => ({ technologySlug })),
      categorySlug: form.categorySlug,
    })
    createdProjectId.value = created.id
    submittedStatus.value = (await submitProjectForReview(created.id)).status
  } catch (error) {
    requestError.value = error.message
  } finally {
    isSubmitting.value = false
  }
}

onMounted(async () => {
  if (!auth.isLoggedIn) return
  try {
    ;[categories.value, technologies.value] = await Promise.all([
      getCategories(),
      getTechnologies(),
    ])
  } catch (error) {
    requestError.value = error.message
  }
})
</script>

<template>
  <section v-if="!auth.isLoggedIn" class="mx-auto max-w-[900px] py-28 text-center">
    <p class="text-body-light dark:text-body-dark">프로젝트를 등록하려면 먼저 로그인해주세요.</p>
    <RouterLink
      to="/login"
      class="mt-5 inline-flex rounded-lg bg-primary-600 px-6 py-3 text-sm font-bold text-white"
      >로그인하러 가기</RouterLink
    >
  </section>

  <section
    v-else-if="createdProjectId"
    data-testid="project-registration-success"
    class="mx-auto max-w-[900px] py-16 text-center"
  >
    <h1 class="font-headline text-3xl font-extrabold text-heading-light dark:text-heading-dark">
      프로젝트 검토 요청이 완료되었습니다.
    </h1>
    <p class="mt-4 text-sm text-body-light dark:text-body-dark">
      {{ form.title }} · 현재 상태 {{ submittedStatus }}
    </p>
    <p class="mt-2 text-sm text-body-light dark:text-body-dark">
      관리자 승인 후 공개 피드와 랭킹에 표시됩니다.
    </p>
    <div class="mt-7 flex justify-center gap-3">
      <a
        :href="form.siteUrl"
        target="_blank"
        rel="noopener"
        class="rounded-lg border border-divider/20 px-5 py-3 text-sm font-semibold"
        >서비스 열기 ↗</a
      >
      <RouterLink
        :to="`/projects/${createdProjectId}`"
        class="rounded-lg bg-primary-600 px-5 py-3 text-sm font-bold text-white"
        >등록한 상세 보기</RouterLink
      >
    </div>
  </section>

  <form v-else class="mx-auto max-w-[900px] space-y-6 pb-14" @submit.prevent="submitProject">
    <header class="border-b border-divider/20 pb-6 dark:border-divider/30">
      <h1 class="font-headline text-3xl font-extrabold text-heading-light dark:text-heading-dark">
        새 프로젝트 등록하기
      </h1>
      <p class="mt-2 text-sm text-body-light dark:text-body-dark">
        실제로 배포된 프로젝트 정보를 저장하고 검토를 요청합니다.
      </p>
    </header>
    <p v-if="requestError" role="alert" class="text-sm text-danger">{{ requestError }}</p>

    <section
      class="grid gap-5 rounded-2xl border border-divider/20 bg-white p-6 sm:grid-cols-2 dark:border-divider/30 dark:bg-surface-dark-1"
    >
      <label class="text-sm font-semibold"
        >프로젝트 이름<input
          v-model="form.title"
          name="title"
          required
          maxlength="160"
          class="mt-2 w-full rounded-lg border border-divider/20 bg-base-light px-3 py-2 text-heading-light dark:border-divider/30 dark:bg-base-dark dark:text-heading-dark"
      /></label>
      <label class="text-sm font-semibold"
        >대표 카테고리<select
          v-model="form.categorySlug"
          name="category"
          required
          class="mt-2 w-full rounded-lg border border-divider/20 bg-base-light px-3 py-2 text-heading-light dark:border-divider/30 dark:bg-base-dark dark:text-heading-dark"
        >
          <option value="" disabled>선택</option>
          <option v-for="item in categories" :key="item.id" :value="item.slug">
            {{ item.name }}
          </option>
        </select></label
      >
      <label class="text-sm font-semibold sm:col-span-2"
        >프로젝트 소개<textarea
          v-model="form.description"
          name="description"
          required
          maxlength="500"
          rows="5"
          class="mt-2 w-full rounded-lg border border-divider/20 bg-base-light px-3 py-2 text-heading-light dark:border-divider/30 dark:bg-base-dark dark:text-heading-dark"
        />
      </label>
      <label class="text-sm font-semibold"
        >서비스 URL<input
          v-model="form.siteUrl"
          name="siteUrl"
          required
          type="url"
          class="mt-2 w-full rounded-lg border border-divider/20 bg-base-light px-3 py-2 text-heading-light dark:border-divider/30 dark:bg-base-dark dark:text-heading-dark"
          placeholder="https://"
      /></label>
      <label class="text-sm font-semibold"
        >GitHub 저장소 URL<input
          v-model="form.repositoryUrl"
          type="url"
          class="mt-2 w-full rounded-lg border border-divider/20 bg-base-light px-3 py-2 text-heading-light dark:border-divider/30 dark:bg-base-dark dark:text-heading-dark"
          placeholder="https://github.com/..."
      /></label>
      <label class="text-sm font-semibold"
        >가격 정책<select
          v-model="form.pricing"
          class="mt-2 w-full rounded-lg border border-divider/20 bg-base-light px-3 py-2 text-heading-light dark:border-divider/30 dark:bg-base-dark dark:text-heading-dark"
        >
          <option value="UNKNOWN">미정</option>
          <option value="FREE">무료</option>
          <option value="FREEMIUM">부분 유료</option>
          <option value="PAID">유료</option>
        </select></label
      >
      <div class="sm:col-span-2">
        <label class="text-sm font-semibold">검색 태그</label>
        <div class="mt-2 flex flex-wrap gap-2">
          <span
            v-for="tag in form.tags"
            :key="tag"
            class="inline-flex items-center gap-1 rounded-full bg-primary-50 px-3 py-1 text-xs"
            >#{{ tag }}
            <button
              type="button"
              :aria-label="`${tag} 태그 삭제`"
              class="text-primary-700 hover:text-primary-900"
              @click="removeTag(tag)"
            >
              ×
            </button></span
          ><input
            v-model="tagDraft"
            class="min-w-48 rounded-lg border border-divider/20 px-3 py-2 text-sm"
            placeholder="입력 후 Enter"
            @keydown.enter.prevent="addTag"
          />
        </div>
      </div>
    </section>

    <section
      class="rounded-2xl border border-divider/20 bg-white p-6 dark:border-divider/30 dark:bg-surface-dark-1"
    >
      <h2 class="font-headline text-lg font-bold text-heading-light dark:text-heading-dark">
        기술 스택
      </h2>
      <div v-for="(items, group) in technologyGroups" :key="group" class="mt-4">
        <h3 class="text-xs font-bold text-body-light dark:text-body-dark">{{ group }}</h3>
        <div class="mt-2 flex flex-wrap gap-2">
          <button
            v-for="item in items"
            :key="item.id"
            type="button"
            :aria-pressed="form.technologySlugs.includes(item.slug)"
            class="rounded-full border border-divider/20 px-3 py-1.5 text-xs text-heading-light dark:border-divider/30 dark:text-heading-dark"
            :class="
              form.technologySlugs.includes(item.slug) &&
              'border-primary-500 bg-primary-50 text-primary-700 dark:bg-primary-900 dark:text-primary-100'
            "
            @click="toggleTechnology(item.slug)"
          >
            {{ item.name }}
          </button>
        </div>
      </div>
      <p class="mt-5 text-xs text-body-light dark:text-body-dark">
        이미지 업로드와 URL 자동 분석은 MVP1 범위에 포함되지 않습니다.
      </p>
    </section>

    <label class="flex items-start gap-3 rounded-xl bg-primary-50 p-4 text-sm"
      ><input v-model="form.agreed" name="agreed" required type="checkbox" class="mt-0.5" />접속
      가능한 프로젝트이며 등록 정책을 준수합니다.</label
    >
    <div class="flex justify-end gap-3">
      <RouterLink
        to="/mypage"
        class="rounded-lg border border-divider/20 px-5 py-3 text-sm font-semibold"
        >취소</RouterLink
      ><button
        type="submit"
        :disabled="isSubmitting"
        class="rounded-lg bg-primary-600 px-6 py-3 text-sm font-bold text-white"
      >
        {{ isSubmitting ? '등록 중...' : '프로젝트 등록 및 검토 요청 →' }}
      </button>
    </div>
  </form>
</template>
