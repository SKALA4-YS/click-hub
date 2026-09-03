<script setup>
import { onMounted, reactive, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

import { getCategories, getTechnologies } from '@/api/catalog'
import { getProject, updateProject } from '@/api/projects'

const route = useRoute()
const categories = ref([])
const technologies = ref([])
const isLoading = ref(true)
const isSaving = ref(false)
const saved = ref(false)
const errorMessage = ref('')
const form = reactive({
  title: '',
  description: '',
  siteUrl: '',
  repositoryUrl: '',
  pricing: 'UNKNOWN',
  categorySlug: '',
  tags: '',
  thumbnailUrl: '',
  technologySlugs: [],
})

async function load() {
  isLoading.value = true
  try {
    const [project, categoryItems, technologyItems] = await Promise.all([
      getProject(route.params.id),
      getCategories(),
      getTechnologies(),
    ])
    categories.value = categoryItems
    technologies.value = technologyItems
    Object.assign(form, {
      title: project.title,
      description: project.description,
      siteUrl: project.siteUrl,
      repositoryUrl: project.repositoryUrl ?? '',
      pricing: project.pricing,
      categorySlug: project.categorySlug ?? '',
      tags: (project.tags ?? []).join(', '),
      thumbnailUrl: project.thumbnailUrl ?? '',
      technologySlugs: (project.techStacks ?? []).map((item) => item.technologySlug),
    })
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

function toggleTechnology(slug) {
  form.technologySlugs = form.technologySlugs.includes(slug)
    ? form.technologySlugs.filter((item) => item !== slug)
    : [...form.technologySlugs, slug]
}

async function save() {
  isSaving.value = true
  saved.value = false
  errorMessage.value = ''
  try {
    await updateProject(route.params.id, {
      title: form.title.trim(),
      description: form.description.trim(),
      siteUrl: form.siteUrl.trim(),
      repositoryUrl: form.repositoryUrl.trim() || null,
      pricing: form.pricing,
      tags: form.tags
        .split(',')
        .map((tag) => tag.trim())
        .filter(Boolean),
      thumbnailUrl: form.thumbnailUrl.trim() || null,
      screenshots: [],
      techStacks: form.technologySlugs.map((technologySlug) => ({ technologySlug })),
      categorySlug: form.categorySlug || null,
    })
    saved.value = true
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isSaving.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="mx-auto max-w-[800px] pb-14">
    <p v-if="isLoading" class="py-20 text-center text-sm text-body-light">
      프로젝트를 불러오는 중입니다.
    </p>
    <form
      v-else
      class="space-y-5 rounded-2xl border border-divider/20 bg-white p-6"
      @submit.prevent="save"
    >
      <h1 class="font-headline text-2xl font-extrabold">프로젝트 수정</h1>
      <p v-if="errorMessage" role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <p v-if="saved" role="status" class="text-sm text-emerald-700">저장되었습니다.</p>
      <label class="block text-sm font-semibold"
        >이름<input
          v-model="form.title"
          required
          maxlength="160"
          class="mt-2 w-full rounded-lg border border-divider/20 px-3 py-2"
      /></label>
      <label class="block text-sm font-semibold"
        >소개<textarea
          v-model="form.description"
          required
          rows="5"
          class="mt-2 w-full rounded-lg border border-divider/20 px-3 py-2"
        />
      </label>
      <label class="block text-sm font-semibold"
        >서비스 URL<input
          v-model="form.siteUrl"
          required
          type="url"
          class="mt-2 w-full rounded-lg border border-divider/20 px-3 py-2"
      /></label>
      <label class="block text-sm font-semibold"
        >저장소 URL<input
          v-model="form.repositoryUrl"
          type="url"
          class="mt-2 w-full rounded-lg border border-divider/20 px-3 py-2"
      /></label>
      <label class="block text-sm font-semibold"
        >대표 카테고리<select
          v-model="form.categorySlug"
          class="mt-2 w-full rounded-lg border border-divider/20 px-3 py-2"
        >
          <option value="">없음</option>
          <option v-for="item in categories" :key="item.id" :value="item.slug">
            {{ item.name }}
          </option>
        </select></label
      >
      <label class="block text-sm font-semibold"
        >태그<input
          v-model="form.tags"
          class="mt-2 w-full rounded-lg border border-divider/20 px-3 py-2"
          placeholder="쉼표로 구분"
      /></label>
      <fieldset>
        <legend class="text-sm font-semibold">기술 스택</legend>
        <div class="mt-2 flex flex-wrap gap-2">
          <button
            v-for="item in technologies"
            :key="item.id"
            type="button"
            :aria-pressed="form.technologySlugs.includes(item.slug)"
            class="rounded-full border border-divider/20 px-3 py-1.5 text-xs"
            :class="form.technologySlugs.includes(item.slug) && 'bg-primary-50 text-primary-700'"
            @click="toggleTechnology(item.slug)"
          >
            {{ item.name }}
          </button>
        </div>
      </fieldset>
      <div class="flex justify-end gap-3">
        <RouterLink to="/mypage" class="rounded-lg border border-divider/20 px-5 py-2"
          >취소</RouterLink
        ><button
          type="submit"
          :disabled="isSaving"
          class="rounded-lg bg-primary-600 px-5 py-2 font-bold text-white"
        >
          {{ isSaving ? '저장 중...' : '저장' }}
        </button>
      </div>
    </form>
  </section>
</template>
