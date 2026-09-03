<script setup>
import { computed, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { categoryOptions, technologyCatalog, techGroupLabels } from '@/data/registrationCatalog'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const isSubmitted = ref(false)
const errors = ref({})
const tagDraft = ref('')

const form = reactive({
  title: '',
  description: '',
  siteUrl: '',
  category: '',
  tags: ['Analytics'],
  repositoryUrl: '',
  demoUrl: '',
  techStack: ['next-js', 'tailwind-css', 'typescript', 'postgresql', 'vercel'],
  agreed: false,
})

const fieldClass =
  'mt-2 w-full rounded-lg border border-divider/20 bg-white px-3.5 py-3 text-sm text-heading-light outline-none transition focus:border-primary-500 focus:ring-2 focus:ring-primary-100'

const techGroups = computed(() => {
  const groups = {}
  for (const technology of technologyCatalog) {
    ;(groups[technology.group] ??= []).push(technology)
  }
  return groups
})

const projectSlug = computed(() =>
  form.title
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9가-힣]+/g, '-')
    .replace(/^-|-$/g, ''),
)

function addTag() {
  const value = tagDraft.value.trim()
  if (value && !form.tags.some((tag) => tag.toLowerCase() === value.toLowerCase())) {
    form.tags.push(value)
  }
  tagDraft.value = ''
}

function removeTag(tag) {
  form.tags = form.tags.filter((item) => item !== tag)
}

function toggleTechnology(slug) {
  form.techStack = form.techStack.includes(slug)
    ? form.techStack.filter((item) => item !== slug)
    : [...form.techStack, slug]
}

function validate() {
  const next = {}
  if (!form.title.trim()) next.title = '프로젝트 이름을 입력해주세요.'
  if (!form.description.trim()) next.description = '프로젝트 소개를 입력해주세요.'
  if (!/^https?:\/\/.+/i.test(form.siteUrl.trim()))
    next.siteUrl = 'https://로 시작하는 배포 URL을 입력해주세요.'
  if (!form.category) next.category = '대표 카테고리를 선택해주세요.'
  if (!form.agreed) next.agreed = '프로젝트 등록 정책에 동의해주세요.'
  errors.value = next
  return Object.keys(next).length === 0
}

function submitProject() {
  if (!validate()) return
  isSubmitted.value = true
}
</script>

<template>
  <section v-if="!auth.isLoggedIn" class="mx-auto max-w-[1120px] py-28 text-center">
    <p class="text-body-light">프로젝트를 등록하려면 먼저 로그인해주세요.</p>
    <RouterLink
      to="/login"
      class="mt-5 inline-flex rounded-lg bg-primary-600 px-6 py-3 text-sm font-bold text-white"
    >
      로그인하러 가기
    </RouterLink>
  </section>

  <section
    v-else-if="isSubmitted"
    data-testid="project-registration-success"
    class="mx-auto max-w-[1120px] pb-14"
  >
    <nav class="mb-10 text-xs text-body-light" aria-label="현재 위치">
      홈 <span class="px-2">›</span> 프로젝트 등록 <span class="px-2">›</span>
      <strong class="text-primary-700">등록 완료</strong>
    </nav>

    <header class="text-center">
      <span
        class="mx-auto grid h-16 w-16 place-items-center rounded-full bg-primary-600 text-2xl text-white shadow-lg shadow-primary-200"
        aria-hidden="true"
        >☺</span
      >
      <h1 class="mt-5 font-headline text-3xl font-extrabold tracking-tight text-heading-light">
        축하합니다! 프로젝트가 성공적으로 등록되었습니다!
      </h1>
      <p class="mt-3 text-sm leading-6 text-body-light">
        <strong class="text-heading-light">'{{ form.title }}'</strong>가 Click-Hub 공개 랭킹에
        참여할 준비를 마쳤습니다.<br />
        지금부터 전 세계 인디 메이커와 함께 클릭 데이터와 솔직한 피드백을 얻을 수 있습니다.
      </p>
      <div class="mt-4 flex flex-wrap justify-center gap-2 text-xs font-semibold">
        <span class="rounded-full bg-emerald-50 px-3 py-1.5 text-emerald-700"
          >● 실시간 피드 노출 중 (Live)</span
        >
        <span class="rounded-full bg-blue-50 px-3 py-1.5 text-blue-700"
          >⚡ 48시간 성과 부스트 활성화</span
        >
      </div>
    </header>

    <article
      class="mt-10 grid overflow-hidden rounded-2xl border border-divider/20 bg-white p-6 shadow-sm md:grid-cols-[380px_1fr] md:gap-7"
    >
      <div class="relative min-h-56 overflow-hidden rounded-xl bg-[#07101f] p-5 text-white">
        <div class="flex gap-1.5">
          <span class="h-2.5 w-2.5 rounded-full bg-red-400"></span
          ><span class="h-2.5 w-2.5 rounded-full bg-amber-300"></span
          ><span class="h-2.5 w-2.5 rounded-full bg-emerald-400"></span>
        </div>
        <p class="mt-7 text-xs text-blue-300">devflow-analytics.io</p>
        <p class="mt-8 font-headline text-2xl font-bold">Ship better.<br />Measure faster.</p>
        <span
          class="absolute bottom-4 right-4 rounded-full bg-white px-3 py-1 text-xs font-bold text-heading-light"
          >▲ 1 Upvote</span
        >
      </div>
      <div class="pt-5 md:pt-1">
        <div class="flex flex-wrap items-center justify-between gap-2">
          <span class="rounded bg-primary-50 px-2 py-1 text-[11px] font-bold text-primary-700"
            >개발자 도구 (Dev Tools)</span
          >
          <a :href="form.siteUrl" class="text-xs font-semibold text-blue-600"
            >{{ form.siteUrl }} ↗</a
          >
        </div>
        <h2 class="mt-3 font-headline text-2xl font-extrabold">{{ form.title }}</h2>
        <p class="mt-3 text-sm leading-6 text-body-light">{{ form.description }}</p>
        <div class="mt-5 flex flex-wrap gap-2">
          <span
            v-for="tech in ['Next.js 14', 'Tailwind CSS', 'TypeScript', 'PostgreSQL', 'Vercel']"
            :key="tech"
            class="rounded-md bg-neutral-100 px-2.5 py-1 text-xs"
            >{{ tech }}</span
          >
        </div>
        <div
          class="mt-6 flex items-center justify-between border-t border-divider/15 pt-4 text-xs text-body-light"
        >
          <span>김민준 <strong class="text-heading-light">@alex_dev</strong></span
          ><span>2026.09.03 15:40 등록</span>
        </div>
      </div>
    </article>

    <section class="mt-8" aria-labelledby="launch-benefits">
      <div class="flex items-end justify-between">
        <div>
          <h2 id="launch-benefits" class="font-headline text-xl font-extrabold">
            메이커 런칭 특별 혜택 및 상태
          </h2>
          <p class="mt-1 text-sm text-body-light">
            프로젝트 성공 런칭을 위해 지금 바로 제공되는 부스트 혜택입니다.
          </p>
        </div>
        <span class="text-xs font-semibold text-blue-600">인디 메이커 액셀러레이터 패스</span>
      </div>
      <div class="mt-5 grid gap-4 md:grid-cols-3">
        <article
          v-for="benefit in [
            ['⚡', '48시간 신규 부스트', '남은 부스트 시간', '47시간 50분'],
            ['▥', '실시간 성과 대시보드', '현재 실시간 유입', '1 View'],
            ['♟', '주간 랭킹 자동 진입', '다음 메이커 순위', '1 / 10'],
          ]"
          :key="benefit[1]"
          class="rounded-xl border border-divider/20 bg-white p-5"
        >
          <span class="grid h-9 w-9 place-items-center rounded-lg bg-primary-50 text-primary-700">{{
            benefit[0]
          }}</span>
          <h3 class="mt-4 font-bold">{{ benefit[1] }}</h3>
          <p class="mt-2 min-h-10 text-xs leading-5 text-body-light">
            등록 직후 프로젝트의 초기 도달률과 클릭 데이터를 빠르게 확인할 수 있습니다.
          </p>
          <div
            class="mt-4 flex items-center justify-between border-t border-divider/15 pt-3 text-xs"
          >
            <span class="text-body-light">{{ benefit[2] }}</span
            ><strong>{{ benefit[3] }}</strong>
          </div>
        </article>
      </div>
    </section>

    <section class="mt-8 rounded-2xl border border-divider/20 bg-white p-6">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p class="text-xs font-bold text-primary-700">VIRAL KIT</p>
          <h2 class="mt-2 font-headline text-lg font-extrabold">Click-Hub 공식 뱃지 달기</h2>
          <p class="mt-1 text-sm text-body-light">
            랜딩페이지나 README에 뱃지를 달고 신뢰도를 높여보세요.
          </p>
        </div>
        <div class="rounded-lg bg-[#111827] px-5 py-3 text-xs font-bold text-white">
          FEATURED ON<br /><span class="text-base">Click-Hub ★ 4.0</span>
        </div>
        <code
          class="max-w-md overflow-hidden rounded-lg bg-neutral-100 px-4 py-3 text-xs text-body-light"
          >[Featured on Click-Hub](https://click-hub.io/project/{{ projectSlug }})</code
        >
      </div>
    </section>

    <footer class="mt-8 flex flex-wrap items-center justify-between gap-4">
      <RouterLink to="/rankings" class="text-sm font-semibold">← 프로젝트 둘러보기</RouterLink>
      <div class="flex gap-3">
        <a
          :href="form.siteUrl"
          class="rounded-lg border border-divider/30 px-5 py-3 text-sm font-semibold"
          >등록한 실제 페이지 보기</a
        >
        <RouterLink
          :to="`/projects/${projectSlug}`"
          class="rounded-lg bg-primary-600 px-5 py-3 text-sm font-bold text-white"
          >등록한 상세 페이지 보기 →</RouterLink
        >
      </div>
    </footer>
  </section>

  <form v-else class="mx-auto max-w-[1120px] pb-14" novalidate @submit.prevent="submitProject">
    <nav class="mb-5 text-xs text-body-light" aria-label="현재 위치">
      홈 <span class="px-2">›</span> 프로젝트 등록
    </nav>
    <header class="border-b border-divider/20 pb-7">
      <div class="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 class="font-headline text-3xl font-extrabold tracking-tight">새 프로젝트 등록하기</h1>
          <p class="mt-2 text-sm text-body-light">
            여러분의 서비스를 전 세계 인디 메이커와 잠재 사용자에게 소개해보세요.
          </p>
        </div>
        <span class="text-xs font-semibold text-body-light"
          >임시 저장됨 · 모든 변경사항이 브라우저에 보관됩니다</span
        >
      </div>
      <ol class="mt-7 grid gap-3 text-xs sm:grid-cols-3">
        <li class="rounded-xl border border-primary-500 bg-primary-50 p-4">
          <strong class="block text-primary-700">1. 프로젝트 정보</strong
          ><span class="mt-1 block text-body-light">서비스의 핵심 정보를 입력해요</span>
        </li>
        <li class="rounded-xl border border-divider/20 bg-white p-4">
          <strong class="block">2. 미디어와 기술</strong
          ><span class="mt-1 block text-body-light">화면과 기술 스택을 소개해요</span>
        </li>
        <li class="rounded-xl border border-divider/20 bg-white p-4">
          <strong class="block">3. 검토 후 런칭</strong
          ><span class="mt-1 block text-body-light">등록 즉시 피드에 공개돼요</span>
        </li>
      </ol>
    </header>

    <div class="mt-7 space-y-5">
      <section class="rounded-2xl border border-divider/20 bg-white p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <h2 class="font-headline text-lg font-extrabold">01 배포 URL 및 AI 원클릭 분석</h2>
          <span class="rounded-full bg-primary-50 px-3 py-1 text-xs font-bold text-primary-700"
            >필수 단계</span
          >
        </div>
        <label class="mt-5 block text-sm font-semibold">웹사이트 URL *</label>
        <input
          v-model="form.siteUrl"
          name="siteUrl"
          type="url"
          placeholder="https://your-project.com"
          :class="fieldClass"
        />
        <p v-if="errors.siteUrl" class="mt-2 text-xs text-danger">{{ errors.siteUrl }}</p>
        <div
          class="mt-4 flex flex-wrap items-center justify-between gap-4 rounded-xl bg-primary-50 p-4 text-xs text-body-light"
        >
          <p>
            <strong class="block text-heading-light"
              >AI가 공개 페이지를 읽고 기본 정보를 채울 수 있어요.</strong
            >분석 버튼은 화면 시안용이며 서버 요청은 발생하지 않습니다.
          </p>
          <div class="flex gap-2">
            <button
              type="button"
              class="rounded-lg border border-divider/30 bg-white px-4 py-2 font-semibold"
            >
              직접 입력</button
            ><button type="button" class="rounded-lg bg-primary-600 px-4 py-2 font-bold text-white">
              AI 원클릭 분석
            </button>
          </div>
        </div>
      </section>

      <section class="rounded-2xl border border-divider/20 bg-white p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <h2 class="font-headline text-lg font-extrabold">02 프로젝트 기본 정보</h2>
          <span class="text-xs text-body-light">입력할수록 발견 가능성이 높아져요</span>
        </div>
        <div class="mt-5 grid gap-5 md:grid-cols-2">
          <label class="block text-sm font-semibold"
            >프로젝트 이름 *<input
              v-model="form.title"
              name="title"
              type="text"
              placeholder="예: DevFlow Analytics"
              :class="fieldClass"
            /><span v-if="errors.title" class="mt-2 block text-xs text-danger">{{
              errors.title
            }}</span></label
          >
          <label class="block text-sm font-semibold"
            >대표 카테고리 *<select v-model="form.category" name="category" :class="fieldClass">
              <option value="" disabled>카테고리 선택</option>
              <option
                v-for="category in categoryOptions"
                :key="category.slug"
                :value="category.slug"
              >
                {{ category.name }}
              </option></select
            ><span v-if="errors.category" class="mt-2 block text-xs text-danger">{{
              errors.category
            }}</span></label
          >
        </div>
        <label class="mt-5 block text-sm font-semibold"
          >프로젝트 소개 *<textarea
            v-model="form.description"
            name="description"
            rows="5"
            maxlength="500"
            placeholder="어떤 문제를 해결하고, 누구에게 도움이 되는 프로젝트인지 설명해주세요."
            :class="fieldClass"
          ></textarea
          ><span class="mt-1 flex justify-between text-xs"
            ><span v-if="errors.description" class="text-danger">{{ errors.description }}</span
            ><span class="ml-auto text-body-light">{{ form.description.length }} / 500</span></span
          ></label
        >
        <label class="mt-5 block text-sm font-semibold">검색 키워드 태그</label>
        <div
          class="mt-2 flex min-h-12 flex-wrap items-center gap-2 rounded-lg border border-divider/20 px-3 py-2"
        >
          <span
            v-for="tag in form.tags"
            :key="tag"
            class="rounded-full bg-primary-50 px-3 py-1.5 text-xs font-semibold text-primary-700"
            >#{{ tag }}
            <button type="button" :aria-label="`${tag} 태그 삭제`" @click="removeTag(tag)">
              ×
            </button></span
          >
          <input
            v-model="tagDraft"
            type="text"
            class="min-w-36 flex-1 bg-transparent text-sm outline-none"
            placeholder="태그 입력 후 Enter"
            @keydown.enter.prevent="addTag"
          />
        </div>
      </section>

      <section class="rounded-2xl border border-divider/20 bg-white p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <h2 class="font-headline text-lg font-extrabold">03 썸네일 & 대표 스크린샷</h2>
          <span class="text-xs text-body-light">권장 1280 × 720 (16:9)</span>
        </div>
        <div class="mt-5 grid gap-4 md:grid-cols-[1.45fr_1fr]">
          <div
            class="min-h-56 rounded-xl bg-gradient-to-br from-[#0b1222] via-[#111b31] to-primary-900 p-6 text-white"
          >
            <span class="text-xs text-blue-300">LIVE PREVIEW</span>
            <p class="mt-10 font-headline text-2xl font-extrabold">
              {{ form.title || '프로젝트 대표 화면' }}
            </p>
            <p class="mt-3 max-w-sm text-sm text-blue-100">
              {{ form.description || '서비스의 가장 매력적인 화면을 대표 이미지로 보여주세요.' }}
            </p>
          </div>
          <label
            class="grid min-h-56 cursor-pointer place-items-center rounded-xl border border-dashed border-primary-300 bg-primary-50/40 p-6 text-center"
            ><span
              ><span
                class="mx-auto grid h-11 w-11 place-items-center rounded-full bg-white text-xl text-primary-700"
                >⇧</span
              ><strong class="mt-3 block text-sm">이미지를 끌어놓거나 클릭해 업로드</strong
              ><small class="mt-2 block text-body-light">PNG, JPG · 최대 10MB</small></span
            ><input type="file" accept="image/png,image/jpeg" class="sr-only"
          /></label>
        </div>
      </section>

      <section class="rounded-2xl border border-divider/20 bg-white p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <h2 class="font-headline text-lg font-extrabold">04 코드 저장소 및 외부 링크</h2>
          <span class="text-xs text-body-light">선택 사항</span>
        </div>
        <div class="mt-5 grid gap-5 md:grid-cols-2">
          <label class="block text-sm font-semibold"
            >GitHub 저장소<input
              v-model="form.repositoryUrl"
              type="url"
              placeholder="https://github.com/username/project"
              :class="fieldClass" /></label
          ><label class="block text-sm font-semibold"
            >라이브 데모 / 문서 링크<input
              v-model="form.demoUrl"
              type="url"
              placeholder="https://docs.your-project.com"
              :class="fieldClass"
          /></label>
        </div>
      </section>

      <section class="rounded-2xl border border-divider/20 bg-white p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <h2 class="font-headline text-lg font-extrabold">05 기술 스택</h2>
          <span class="text-xs text-body-light">최대 10개까지 선택</span>
        </div>
        <div
          v-for="(technologies, group) in techGroups"
          :key="group"
          class="mt-5 grid gap-3 md:grid-cols-[150px_1fr]"
        >
          <h3 class="text-xs font-bold uppercase tracking-wider text-body-light">
            {{ techGroupLabels[group] }}
          </h3>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="technology in technologies"
              :key="technology.slug"
              type="button"
              :aria-pressed="form.techStack.includes(technology.slug)"
              class="rounded-full border px-3 py-1.5 text-xs font-semibold transition"
              :class="
                form.techStack.includes(technology.slug)
                  ? 'border-primary-500 bg-primary-50 text-primary-700'
                  : 'border-divider/20 bg-white text-body-light'
              "
              @click="toggleTechnology(technology.slug)"
            >
              {{ technology.name }}
            </button>
          </div>
        </div>
      </section>

      <section class="rounded-2xl border border-primary-200 bg-primary-50/50 p-6">
        <div class="flex flex-wrap items-start justify-between gap-5">
          <div>
            <p class="text-xs font-bold text-primary-700">런칭 전 마지막 확인</p>
            <h2 class="mt-2 font-headline text-lg font-extrabold">
              프로젝트가 실제로 접속 가능한 상태인가요?
            </h2>
            <p class="mt-2 text-sm text-body-light">
              등록 후 48시간 동안 신규 프로젝트 부스트가 적용되며, 공개 피드와 랭킹에 즉시
              표시됩니다.
            </p>
          </div>
          <span class="rounded-lg bg-white px-4 py-2 text-xs font-semibold text-primary-700"
            >예상 공개 상태 · LIVE</span
          >
        </div>
        <label class="mt-5 flex items-start gap-3 rounded-xl bg-white p-4 text-sm"
          ><input
            v-model="form.agreed"
            name="agreed"
            type="checkbox"
            class="mt-0.5 h-4 w-4 accent-primary-600"
          /><span
            >정상 접속 가능한 프로젝트이며 Click-Hub 등록 정책과 커뮤니티 가이드를 준수합니다.<small
              v-if="errors.agreed"
              class="mt-1 block text-danger"
              >{{ errors.agreed }}</small
            ></span
          ></label
        >
      </section>

      <div
        class="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-divider/20 bg-white p-5 shadow-sm"
      >
        <p class="text-xs leading-5 text-body-light">
          등록 버튼을 누르면 별도의 백엔드 요청 없이<br />이 브라우저에서 완료 시안으로 전환됩니다.
        </p>
        <div class="flex gap-3">
          <RouterLink
            to="/rankings"
            class="rounded-lg border border-divider/30 px-5 py-3 text-sm font-semibold"
            >취소</RouterLink
          ><button
            type="submit"
            class="rounded-lg bg-primary-600 px-7 py-3 text-sm font-bold text-white shadow-lg shadow-primary-200"
          >
            프로젝트 등록 및 즉시 배포 →
          </button>
        </div>
      </div>
    </div>
  </form>
</template>
