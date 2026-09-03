<script setup>
import { computed, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { categoryOptions, pricingOptions, technologyCatalog, techGroupLabels } from '@/data/registrationCatalog'

// POST /api/v1/projects 자리. 목업의 "AI 원클릭 분석"은 확정 API 명세에 없어서 뺐고,
// 필드는 api-spec-draft.md 2장 요청 예시 그대로 따른다.
const auth = useAuthStore()

// 폼 필드 전체에서 반복되는 인풋 스타일을 한 곳에 모아서, 디자인 토큰이 바뀌어도 여기만 고치면 되게 한다.
const inputClass =
  'w-full rounded-lg border border-divider/20 bg-surface-light-1 px-3 py-2 text-sm outline-none focus:border-primary-400 dark:border-divider/30 dark:bg-surface-dark-1'

const form = reactive({
  title: '',
  description: '',
  site_url: '',
  repository_url: '',
  thumbnail_url: '',
  pricing: 'UNKNOWN',
  primary_category_slug: '',
  tags: [],
  screenshots: [],
  tech_stack: [], // { technology_slug, group, version }
  agreed: false,
})

const tagDraft = ref('')
const screenshotDraft = reactive({ url: '', alt: '' })
const errors = ref({})
const isSubmitted = ref(false)

const techGroups = computed(() => {
  const groups = {}
  for (const tech of technologyCatalog) {
    ;(groups[tech.group] ??= []).push(tech)
  }
  return groups
})

function isUrl(value) {
  return /^https?:\/\/.+/i.test(value.trim())
}

function addTag() {
  const value = tagDraft.value.trim()
  if (!value) return
  if (!form.tags.some((tag) => tag.toLowerCase() === value.toLowerCase())) {
    form.tags.push(value)
  }
  tagDraft.value = ''
}
function removeTag(tag) {
  form.tags = form.tags.filter((t) => t !== tag)
}

function addScreenshot() {
  if (!isUrl(screenshotDraft.url)) return
  if (form.screenshots.length >= 12) return
  form.screenshots.push({ url: screenshotDraft.url.trim(), alt: screenshotDraft.alt.trim() })
  screenshotDraft.url = ''
  screenshotDraft.alt = ''
}
function removeScreenshot(index) {
  form.screenshots.splice(index, 1)
}

function isTechSelected(slug) {
  return form.tech_stack.some((t) => t.technology_slug === slug)
}
function toggleTech(tech) {
  if (isTechSelected(tech.slug)) {
    form.tech_stack = form.tech_stack.filter((t) => t.technology_slug !== tech.slug)
  } else {
    form.tech_stack.push({ technology_slug: tech.slug, group: tech.group, version: '' })
  }
}

function validate() {
  const next = {}
  if (!form.title.trim()) next.title = '프로젝트 이름을 입력해주세요.'
  if (!form.description.trim()) next.description = '한 줄 소개를 입력해주세요.'
  if (!isUrl(form.site_url)) next.site_url = '실제 접속 가능한 URL(https://...)을 입력해주세요.'
  if (form.repository_url && !isUrl(form.repository_url)) next.repository_url = 'https://로 시작하는 URL을 입력해주세요.'
  if (!form.primary_category_slug) next.primary_category_slug = '대표 카테고리를 선택해주세요.'
  if (!form.agreed) next.agreed = '등록 정책에 동의해주세요.'
  errors.value = next
  return Object.keys(next).length === 0
}

function handleSubmit() {
  if (!validate()) return
  // 실제로는 POST /api/v1/projects (DRAFT 생성) -> POST /api/v1/projects/{id}/submit (PENDING_REVIEW) 순.
  // 지금은 백엔드가 없어 화면 확인용으로 제출 상태만 표시한다.
  isSubmitted.value = true
}

function resetForm() {
  form.title = ''
  form.description = ''
  form.site_url = ''
  form.repository_url = ''
  form.thumbnail_url = ''
  form.pricing = 'UNKNOWN'
  form.primary_category_slug = ''
  form.tags = []
  form.screenshots = []
  form.tech_stack = []
  form.agreed = false
  errors.value = {}
  isSubmitted.value = false
}
</script>

<template>
  <div v-if="!auth.isLoggedIn" class="flex flex-col items-center gap-3 py-24 text-center">
    <p class="text-body-light dark:text-body-dark">프로젝트를 등록하려면 먼저 로그인해주세요.</p>
    <RouterLink to="/login" class="rounded-full bg-primary-600 px-5 py-2 text-sm font-semibold text-white hover:bg-primary-700">
      로그인하러 가기
    </RouterLink>
  </div>

  <div v-else-if="isSubmitted" class="mx-auto flex max-w-lg flex-col items-center gap-4 py-20 text-center">
    <span class="flex h-16 w-16 items-center justify-center rounded-full bg-success/10 text-3xl">📝</span>
    <h1 class="font-headline text-xl font-bold text-heading-light dark:text-heading-dark">등록이 접수됐어요</h1>
    <p class="text-sm text-body-light dark:text-body-dark">
      '{{ form.title }}'이(가) <strong class="text-warning">검수 대기(PENDING_REVIEW)</strong> 상태로 등록됐습니다.
      서비스 URL 접속 확인과 운영팀 검토가 끝나면 홈 피드와 전체 목록에 게시됩니다.
    </p>
    <div class="flex gap-3">
      <RouterLink to="/" class="rounded-full border border-divider/30 px-5 py-2 text-sm font-medium hover:border-primary-400">
        홈으로 돌아가기
      </RouterLink>
      <button type="button" class="rounded-full bg-primary-600 px-5 py-2 text-sm font-semibold text-white hover:bg-primary-700" @click="resetForm">
        새 프로젝트 등록하기
      </button>
    </div>
  </div>

  <form v-else class="mx-auto flex max-w-2xl flex-col gap-8" @submit.prevent="handleSubmit">
    <div>
      <h1 class="font-headline text-2xl font-bold text-heading-light dark:text-heading-dark">새 프로젝트 등록하기</h1>
      <p class="mt-1 text-sm text-body-light dark:text-body-dark">
        실제로 접속 가능한 웹서비스만 등록할 수 있어요. GitHub 저장소 연결은 선택 사항입니다.
      </p>
    </div>

    <!-- 기본 정보 -->
    <section class="flex flex-col gap-4 rounded-xl border border-divider/20 p-5 dark:border-divider/25">
      <h2 class="font-semibold text-heading-light dark:text-heading-dark">기본 정보</h2>

      <div>
        <label class="mb-1 block text-sm font-medium">프로젝트 이름 *</label>
        <input v-model="form.title" type="text" placeholder="예: DevFlow Analytics" :class="inputClass" />
        <p v-if="errors.title" class="mt-1 text-xs text-danger">{{ errors.title }}</p>
      </div>

      <div>
        <label class="mb-1 block text-sm font-medium">한 줄 소개 *</label>
        <textarea v-model="form.description" rows="2" placeholder="이 프로젝트가 어떤 문제를 해결하는지 한두 문장으로 설명해주세요." class="w-full resize-none" :class="inputClass" />
        <p v-if="errors.description" class="mt-1 text-xs text-danger">{{ errors.description }}</p>
      </div>

      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm font-medium">서비스 배포 URL *</label>
          <input v-model="form.site_url" type="text" placeholder="https://your-service.com" :class="inputClass" />
          <p v-if="errors.site_url" class="mt-1 text-xs text-danger">{{ errors.site_url }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm font-medium">GitHub 저장소 URL (선택)</label>
          <input v-model="form.repository_url" type="text" placeholder="https://github.com/me/project" :class="inputClass" />
          <p v-if="errors.repository_url" class="mt-1 text-xs text-danger">{{ errors.repository_url }}</p>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm font-medium">대표 카테고리 *</label>
          <select v-model="form.primary_category_slug" :class="inputClass">
            <option value="" disabled>카테고리 선택</option>
            <option v-for="c in categoryOptions" :key="c.slug" :value="c.slug">{{ c.name }}</option>
          </select>
          <p v-if="errors.primary_category_slug" class="mt-1 text-xs text-danger">{{ errors.primary_category_slug }}</p>
        </div>
        <div>
          <label class="mb-1 block text-sm font-medium">가격 정책</label>
          <select v-model="form.pricing" :class="inputClass">
            <option v-for="p in pricingOptions" :key="p.value" :value="p.value">{{ p.label }}</option>
          </select>
        </div>
      </div>

      <div>
        <label class="mb-1 block text-sm font-medium">검색 키워드 태그</label>
        <div class="flex flex-wrap items-center gap-2 rounded-lg border border-divider/20 p-2 dark:border-divider/30">
          <span v-for="tag in form.tags" :key="tag" class="flex items-center gap-1 rounded-full bg-primary-50 px-2.5 py-1 text-xs font-medium text-primary-700 dark:bg-primary-900 dark:text-primary-100">
            #{{ tag }}
            <button type="button" class="text-primary-400 hover:text-primary-700" @click="removeTag(tag)">×</button>
          </span>
          <input
            v-model="tagDraft"
            type="text"
            placeholder="태그 입력 후 Enter"
            class="min-w-[120px] flex-1 bg-transparent px-1 py-1 text-sm outline-none"
            @keydown.enter.prevent="addTag"
          />
        </div>
      </div>
    </section>

    <!-- 썸네일 & 스크린샷 -->
    <section class="flex flex-col gap-4 rounded-xl border border-divider/20 p-5 dark:border-divider/25">
      <h2 class="font-semibold text-heading-light dark:text-heading-dark">썸네일 & 스크린샷</h2>

      <div>
        <label class="mb-1 block text-sm font-medium">썸네일 이미지 URL</label>
        <input v-model="form.thumbnail_url" type="text" placeholder="https://.../thumbnail.png" :class="inputClass" />
        <p class="mt-1 text-xs text-body-light dark:text-body-dark">홈 피드 카드와 상세 페이지 상단에 노출됩니다.</p>
      </div>

      <div>
        <label class="mb-1 block text-sm font-medium">스크린샷 (최대 12장)</label>
        <div class="flex flex-col gap-2">
          <div v-for="(shot, index) in form.screenshots" :key="shot.url" class="flex items-center gap-2 text-sm">
            <span class="flex-1 truncate rounded-lg bg-neutral-100 px-3 py-1.5 dark:bg-surface-dark-2">{{ shot.url }}</span>
            <span class="w-32 truncate text-xs text-body-light dark:text-body-dark">{{ shot.alt || '설명 없음' }}</span>
            <button type="button" class="text-danger hover:underline" @click="removeScreenshot(index)">삭제</button>
          </div>
          <div class="flex gap-2">
            <input v-model="screenshotDraft.url" type="text" placeholder="https://.../screenshot.png" class="flex-1" :class="inputClass" />
            <input v-model="screenshotDraft.alt" type="text" placeholder="설명 (선택)" class="w-40" :class="inputClass" />
            <button type="button" class="rounded-lg border border-divider/30 px-3 py-2 text-sm font-medium hover:border-primary-400" @click="addScreenshot">
              추가
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 기술 스택 -->
    <section class="flex flex-col gap-4 rounded-xl border border-divider/20 p-5 dark:border-divider/25">
      <h2 class="font-semibold text-heading-light dark:text-heading-dark">기술 스택</h2>
      <div v-for="(techs, group) in techGroups" :key="group" class="flex flex-col gap-2">
        <span class="text-xs font-semibold uppercase text-body-light dark:text-body-dark">{{ techGroupLabels[group] }}</span>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="tech in techs"
            :key="tech.slug"
            type="button"
            class="rounded-full border px-3 py-1.5 text-sm transition-colors"
            :class="
              isTechSelected(tech.slug)
                ? 'border-primary-600 bg-primary-600 text-white'
                : 'border-divider/20 text-body-light hover:border-primary-300 dark:border-divider/30 dark:text-body-dark'
            "
            @click="toggleTech(tech)"
          >
            {{ tech.name }}
          </button>
        </div>
      </div>
    </section>

    <!-- 약관 & 제출 -->
    <section class="flex flex-col gap-4">
      <label class="flex items-start gap-2 text-sm text-body-light dark:text-body-dark">
        <input v-model="form.agreed" type="checkbox" class="mt-0.5" />
        실제 정상 접속 및 동작이 가능한 웹 서비스임을 보증하며, Click HUB 등록 정책을 준수합니다.
      </label>
      <p v-if="errors.agreed" class="text-xs text-danger">{{ errors.agreed }}</p>

      <button type="submit" class="self-start rounded-full bg-primary-600 px-6 py-3 text-sm font-semibold text-white hover:bg-primary-700">
        프로젝트 등록하기
      </button>
    </section>
  </form>
</template>
