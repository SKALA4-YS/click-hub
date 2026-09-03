<script setup>
import { computed, ref } from 'vue'

import {
  faqItems,
  promptTemplates,
  roadmapFilters,
  roadmapSteps,
  showcases,
  starterTools,
} from '@/data/tutorials'

const selectedFilter = ref('all')

const visibleSteps = computed(() =>
  roadmapSteps.filter((step) => selectedFilter.value === 'all' || step.id === selectedFilter.value),
)
</script>

<template>
  <div class="mx-auto max-w-[1180px] pb-8">
    <nav
      aria-label="현재 위치"
      class="mb-8 text-xs font-semibold text-body-light dark:text-body-dark"
    >
      홈 <span class="mx-1.5 text-divider">/</span> 튜토리얼
      <span class="mx-1.5 text-divider">/</span> 바이브 코딩 실전 가이드
    </nav>

    <section
      class="relative overflow-hidden rounded-2xl border border-divider/20 bg-surface-light-1 px-7 py-9 shadow-sm dark:border-divider/25 dark:bg-surface-dark-1 sm:px-10"
    >
      <div
        class="absolute -right-24 -top-36 h-80 w-80 rounded-full bg-primary-200/40 blur-3xl dark:bg-primary-500/15"
      />
      <div class="relative">
        <span
          class="inline-flex rounded-full bg-primary-50 px-3 py-1 text-xs font-bold text-primary-700 dark:bg-primary-900/60 dark:text-primary-200"
          >AI 바이브 코딩 실전 마스터 코스</span
        >
        <div class="mt-4 flex flex-col gap-6 xl:flex-row xl:items-center xl:justify-between">
          <div class="max-w-3xl">
            <h1
              class="font-headline text-3xl font-extrabold leading-tight tracking-tight text-heading-light dark:text-heading-dark sm:text-4xl"
            >
              아이디어만으로 웹사이트 런칭까지, 바이브 코딩 (Vibe Coding) 완벽 가이드
            </h1>
            <p class="mt-4 text-sm leading-7 text-body-light dark:text-body-dark">
              코딩을 몰라도, 프론트엔드가 막연해도 괜찮습니다. AI와 자연어로 대화하며 상상 속 제품을
              현실로 구현하는 5단계 실전 로드맵과 복붙용 프롬프트 템플릿을 제공합니다.
            </p>
          </div>
          <div class="flex shrink-0 flex-wrap gap-3">
            <button
              type="button"
              class="rounded-xl bg-primary-600 px-5 py-3 text-sm font-bold text-white shadow-sm transition-colors hover:bg-primary-700"
            >
              1강부터 바로 시작하기
            </button>
            <button
              type="button"
              class="rounded-xl border border-divider/25 bg-white px-5 py-3 text-sm font-bold text-heading-light transition-colors hover:bg-primary-50 dark:border-divider/35 dark:bg-surface-dark-2 dark:text-heading-dark dark:hover:bg-primary-900"
            >
              실전 프롬프트 템플릿 복사
            </button>
          </div>
        </div>
        <div
          class="mt-6 flex flex-wrap gap-x-5 gap-y-2 text-xs font-medium text-body-light dark:text-body-dark"
        >
          <span>총 5개 실전 챕터</span><span>완료 제작 소요 3시간</span><span>비전공자 친화적</span>
        </div>
        <div class="mt-7 flex flex-wrap gap-2" role="group" aria-label="로드맵 필터">
          <button
            v-for="filter in roadmapFilters"
            :key="filter.id"
            type="button"
            :aria-label="`${filter.label} 필터`"
            :aria-pressed="selectedFilter === filter.id"
            class="rounded-xl border px-4 py-2 text-sm font-semibold transition-colors"
            :class="
              selectedFilter === filter.id
                ? 'border-primary-600 bg-primary-600 text-white'
                : 'border-divider/20 bg-surface-light-1 text-body-light hover:border-primary-300 hover:text-primary-700 dark:border-divider/30 dark:bg-surface-dark-2 dark:text-body-dark dark:hover:text-primary-200'
            "
            @click="selectedFilter = filter.id"
          >
            {{ filter.label
            }}<span
              v-if="filter.count"
              class="ml-1.5 rounded-md bg-white/20 px-1.5 py-0.5 text-xs"
              >{{ filter.count }}</span
            >
          </button>
        </div>
      </div>
    </section>

    <div class="mt-8 grid gap-8 lg:grid-cols-[minmax(0,1fr)_320px]">
      <main>
        <div class="mb-5 flex items-center justify-between">
          <h2
            class="border-l-4 border-primary-600 pl-3 font-headline text-xl font-extrabold text-heading-light dark:text-heading-dark"
          >
            단계별 실전 바이브 코딩 커리큘럼
          </h2>
          <span class="text-sm text-body-light dark:text-body-dark"
            >총 {{ visibleSteps.length }}단계 완성 코스</span
          >
        </div>
        <div class="space-y-4">
          <article
            v-for="step in visibleSteps"
            :key="step.step"
            data-testid="roadmap-card"
            class="rounded-2xl border bg-surface-light-1 p-6 shadow-sm transition-colors dark:bg-surface-dark-1"
            :class="
              step.recommended
                ? 'border-primary-400 bg-primary-50/25 dark:bg-primary-950/25'
                : 'border-divider/20 dark:border-divider/25'
            "
          >
            <div class="flex items-center justify-between gap-3">
              <div class="flex flex-wrap items-center gap-2">
                <span
                  class="rounded-md bg-primary-50 px-2.5 py-1 text-xs font-bold text-primary-700 dark:bg-primary-900/65 dark:text-primary-200"
                  >{{ step.category }}</span
                ><span class="text-xs font-semibold text-body-light dark:text-body-dark">{{
                  step.step
                }}</span>
              </div>
              <span class="rounded-full bg-success/10 px-2.5 py-1 text-xs font-bold text-success">{{
                step.recommended ? '추천 베스트' : '무료 공개'
              }}</span>
            </div>
            <h3
              class="mt-4 font-headline text-xl font-bold tracking-tight text-heading-light dark:text-heading-dark"
            >
              {{ step.title }}
            </h3>
            <p class="mt-2 text-sm leading-6 text-body-light dark:text-body-dark">
              {{ step.description }}
            </p>
            <div
              class="mt-5 flex flex-wrap items-center gap-x-4 gap-y-2 border-t border-divider/15 pt-4 text-xs text-body-light dark:border-divider/25 dark:text-body-dark"
            >
              <span>소요시간 {{ step.duration }}</span
              ><span>도구: {{ step.tools }}</span
              ><span class="font-semibold text-primary-700 dark:text-primary-200">{{
                step.outcome
              }}</span
              ><button
                type="button"
                class="ml-auto rounded-lg border border-divider/25 px-3 py-1.5 font-semibold text-heading-light hover:bg-primary-50 dark:border-divider/35 dark:text-heading-dark dark:hover:bg-primary-900/50"
              >
                {{ step.recommended ? '이어서 학습하기' : '학습하기' }}
              </button>
            </div>
          </article>
        </div>

        <section class="mt-10">
          <div class="mb-5 flex items-center justify-between">
            <h2
              class="border-l-4 border-primary-500 pl-3 font-headline text-xl font-extrabold text-heading-light dark:text-heading-dark"
            >
              그대로 복사해 쓰는 실전 프롬프트 템플릿
            </h2>
            <span class="text-sm text-body-light dark:text-body-dark">원클릭 복사 가능</span>
          </div>
          <div class="space-y-4">
            <article
              v-for="([title, prompt], index) in promptTemplates"
              :key="title"
              class="rounded-2xl border border-divider/20 bg-surface-light-1 p-5 shadow-sm dark:border-divider/25 dark:bg-surface-dark-1"
            >
              <div class="flex items-center justify-between gap-3">
                <h3 class="text-sm font-bold text-heading-light dark:text-heading-dark">
                  <span
                    class="mr-2 inline-flex h-6 w-6 items-center justify-center rounded-md bg-primary-50 text-primary-700 dark:bg-primary-900/60 dark:text-primary-200"
                    >{{ index + 1 }}</span
                  >{{ title }}
                </h3>
                <button
                  type="button"
                  class="rounded-lg border border-primary-200 px-3 py-1.5 text-xs font-bold text-primary-700 hover:bg-primary-50 dark:border-primary-700 dark:text-primary-200"
                >
                  프롬프트 복사
                </button>
              </div>
              <p
                class="mt-4 rounded-xl border border-divider/15 bg-base-light px-4 py-3 text-xs leading-5 text-body-light dark:border-divider/20 dark:bg-base-dark dark:text-body-dark"
              >
                {{ prompt }}
              </p>
            </article>
          </div>
        </section>

        <section class="mt-10">
          <div class="mb-5 flex items-center justify-between">
            <h2
              class="border-l-4 border-success pl-3 font-headline text-xl font-extrabold text-heading-light dark:text-heading-dark"
            >
              바이브 코딩으로 런칭한 메이커들의 실제 쇼케이스
            </h2>
            <button
              type="button"
              class="text-sm font-semibold text-primary-700 dark:text-primary-200"
            >
              더 많은 쇼케이스 보기
            </button>
          </div>
          <div class="grid gap-4 sm:grid-cols-2">
            <article
              v-for="[title, author, description, metric] in showcases"
              :key="title"
              class="rounded-2xl border border-divider/20 bg-surface-light-1 p-5 shadow-sm dark:border-divider/25 dark:bg-surface-dark-1"
            >
              <h3 class="font-bold text-heading-light dark:text-heading-dark">{{ title }}</h3>
              <p class="mt-1 text-xs text-body-light dark:text-body-dark">{{ author }}</p>
              <p class="mt-4 text-sm leading-6 text-body-light dark:text-body-dark">
                “{{ description }}”
              </p>
              <p
                class="mt-4 border-t border-divider/15 pt-3 text-sm font-extrabold text-success dark:border-divider/25"
              >
                {{ metric }}
              </p>
            </article>
          </div>
        </section>
      </main>

      <aside class="space-y-5">
        <section
          data-testid="tutorial-progress"
          class="rounded-2xl border border-divider/20 bg-surface-light-1 p-5 shadow-sm dark:border-divider/25 dark:bg-surface-dark-1"
        >
          <div class="flex items-center justify-between">
            <h2 class="font-bold text-heading-light dark:text-heading-dark">
              나의 튜토리얼 진행률
            </h2>
            <span class="font-extrabold text-primary-600">40%</span>
          </div>
          <div
            class="mt-5 flex items-center justify-between text-sm font-semibold text-body-light dark:text-body-dark"
          >
            <span>2 / 5 완료</span><span>남은 시간 약 1시간 45분</span>
          </div>
          <div class="mt-2 h-2 overflow-hidden rounded-full bg-primary-100 dark:bg-primary-900">
            <div class="h-full w-2/5 rounded-full bg-primary-500" />
          </div>
          <p class="mt-4 text-xs leading-5 text-body-light dark:text-body-dark">
            현재 Step 3. Cursor IDE 실전 테크닉을 수강할 차례입니다.
          </p>
          <button
            type="button"
            class="mt-4 w-full rounded-xl bg-primary-600 px-4 py-2.5 text-sm font-bold text-white hover:bg-primary-700"
          >
            Step 3 이어서 학습하기
          </button>
        </section>
        <section
          class="rounded-2xl border border-divider/20 bg-surface-light-1 p-5 shadow-sm dark:border-divider/25 dark:bg-surface-dark-1"
        >
          <h2 class="font-bold text-heading-light dark:text-heading-dark">
            바이브 코딩 추천 도구 스타터 팩
          </h2>
          <div class="mt-4 divide-y divide-divider/15 dark:divide-divider/25">
            <div
              v-for="tool in starterTools"
              :key="tool.name"
              class="flex items-center justify-between gap-3 py-3"
            >
              <div>
                <h3 class="text-sm font-bold text-heading-light dark:text-heading-dark">
                  {{ tool.name }}
                </h3>
                <p class="mt-1 text-xs text-body-light dark:text-body-dark">{{ tool.detail }}</p>
              </div>
              <span
                class="shrink-0 rounded-md bg-primary-50 px-2 py-1 text-[11px] font-bold text-primary-700 dark:bg-primary-900/60 dark:text-primary-200"
                >{{ tool.tag }}</span
              >
            </div>
          </div>
        </section>
        <section
          class="rounded-2xl border border-divider/20 bg-surface-light-1 p-5 shadow-sm dark:border-divider/25 dark:bg-surface-dark-1"
        >
          <h2 class="font-bold text-heading-light dark:text-heading-dark">자주 묻는 질문 (FAQ)</h2>
          <div class="mt-4 space-y-3">
            <article
              v-for="[question, answer] in faqItems"
              :key="question"
              class="rounded-xl border border-divider/15 bg-base-light p-3 dark:border-divider/20 dark:bg-base-dark"
            >
              <h3 class="text-xs font-bold leading-5 text-heading-light dark:text-heading-dark">
                Q. {{ question }}
              </h3>
              <p class="mt-2 text-xs leading-5 text-body-light dark:text-body-dark">{{ answer }}</p>
            </article>
          </div>
        </section>
        <section
          class="rounded-2xl bg-gradient-to-br from-secondary via-primary-700 to-blue-500 p-6 text-white shadow-sm"
        >
          <p class="text-xs font-bold text-primary-100">Q&amp;A</p>
          <h2 class="mt-3 font-headline text-xl font-extrabold">혼자 끙끙 앓지 마세요!</h2>
          <p class="mt-3 text-sm leading-6 text-primary-100">
            현직 메이커와 튜토리얼 마스터들이 Click HUB 커뮤니티에서 실시간 답변 대기 중입니다.
          </p>
          <RouterLink
            to="/community"
            class="mt-5 inline-flex rounded-xl bg-white px-4 py-2.5 text-sm font-bold text-primary-700"
            >커뮤니티에 질문하기</RouterLink
          >
        </section>
      </aside>
    </div>
  </div>
</template>
