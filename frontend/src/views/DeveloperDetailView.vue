<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'

const activeTab = ref('projects')

const tabs = [
  { id: 'projects', label: '내 프로젝트 관리', count: 3 },
  { id: 'activity', label: '활동 내역 / 통계' },
  { id: 'rewards', label: '알림 설정' },
  { id: 'account', label: '계정 및 보안' },
]

const projects = [
  {
    id: 'prj_301',
    icon: 'ϟ',
    title: 'DevFlow Analytics',
    status: '운영중 (Active)',
    badge: '주간 Top 1위',
    description:
      '인디 SaaS를 위한 초경량 웹 비이탈 및 사용자 리텐션 실시간 집계 툴. 복잡한 GA4 대신 1초 스크립트로 핵심 퍼널을 확인하세요.',
    tags: ['개발자 도구', '#SaaS', '#Next.js', '#TypeScript'],
    metrics: ['12,410 외부 클릭', '1,204 북마크', '20 피드백'],
    growth: '+18.4%',
  },
  {
    id: 'prj_302',
    icon: '▣',
    title: 'PromptCraft Studio',
    status: 'Active',
    badge: 'Top 12',
    description: 'LLM 프롬프트를 버전 관리하고 A/B 테스트하는 개발자 협업 플레이그라운드입니다.',
    tags: ['AI 개발', '#Prompt', '#OpenAPI'],
    metrics: ['8,120 외부 클릭', '942 북마크', '14 피드백'],
    growth: '+9.2%',
  },
  {
    id: 'prj_303',
    icon: '▤',
    title: 'IconGenie Studio',
    status: '검토중 (Under Review)',
    badge: '디자인 2위',
    description:
      'SVG 아이콘을 검색하고 React/Vue 컴포넌트 코드로 바로 복사하는 디자이너·개발자 협업 데스크톱 앱.',
    tags: ['디자인', '#SVG', '#Figma'],
    metrics: ['심사 진행 중', '등록 2시간 전', '피드백 대기'],
    growth: '검토 중',
  },
]

const activity = [
  ['오늘 14:30', 'DevFlow Analytics가 주간 랭킹 1위에 올랐습니다.'],
  ['어제 18:12', 'PromptCraft Studio에 새로운 피드백이 도착했습니다.'],
  ['9월 1일', 'IconGenie Studio를 새 프로젝트로 등록했습니다.'],
]
</script>

<template>
  <section class="mx-auto max-w-[1120px] pb-14" aria-labelledby="developer-detail-heading">
    <nav class="mb-5 text-xs text-body-light" aria-label="현재 위치">
      홈 <span class="px-2">›</span> 마이페이지
    </nav>

    <header class="rounded-2xl border border-divider/20 bg-white p-6 shadow-sm">
      <div class="flex flex-wrap items-center gap-5">
        <span
          class="grid h-20 w-20 shrink-0 place-items-center rounded-full bg-primary-50 text-xl font-extrabold text-secondary"
          >AK</span
        >
        <div class="min-w-0 flex-1">
          <div class="flex flex-wrap items-center gap-2">
            <h1 id="developer-detail-heading" class="font-headline text-2xl font-extrabold">
              김민준 <small class="text-sm font-medium text-body-light">(Alex Kim)</small>
            </h1>
            <span class="rounded-full bg-primary-50 px-2.5 py-1 text-xs font-bold text-primary-700"
              >@alex_dev</span
            >
          </div>
          <p class="mt-2 max-w-2xl text-sm leading-6 text-body-light">
            풀스택 인디 개발자 / 1인 SaaS 빌더. 실시간 성능 모니터링 DevFlow 운영 중. 쓸모 있고
            단단한 웹 도구를 직접 만듭니다.
          </p>
          <div class="mt-3 flex flex-wrap gap-4 text-xs text-body-light">
            <a href="https://github.com" class="hover:text-primary-600">◈ github.com/alex-dev ↗</a
            ><a href="https://x.com" class="hover:text-primary-600">𝕏 x.com/alex-maker ↗</a
            ><span>⌖ blog.alexkim.dev</span>
          </div>
        </div>
        <div class="flex gap-2">
          <RouterLink
            to="/projects/new"
            class="rounded-lg bg-primary-600 px-5 py-3 text-sm font-bold text-white"
            >＋ 새 사이트 등록하기</RouterLink
          ><button
            type="button"
            class="rounded-lg border border-divider/30 px-4 py-3 text-sm font-semibold"
          >
            연필 프로필 수정
          </button>
        </div>
      </div>
    </header>

    <section class="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-4" aria-label="메이커 성과 요약">
      <article
        v-for="metric in [
          ['▣', '등록한 사이트', '3개', '운영 2 · 심사 1'],
          ['▤', '웹 클릭수', '3,840', '+12% 이번 주'],
          ['▱', '총 누적 조회수', '48.2k', '+3.8k 증가'],
          ['♡', '피드백 & 코멘트', '1.2k', '14 새 피드백'],
        ]"
        :key="metric[1]"
        class="rounded-xl border border-divider/20 bg-white p-5 shadow-sm"
      >
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-body-light">{{ metric[1] }}</span
          ><span
            class="grid h-8 w-8 place-items-center rounded-lg bg-primary-50 text-primary-700"
            >{{ metric[0] }}</span
          >
        </div>
        <strong class="mt-4 block font-headline text-2xl">{{ metric[2] }}</strong
        ><span class="mt-2 block text-xs text-emerald-600">{{ metric[3] }}</span>
      </article>
    </section>

    <div
      class="mt-6 flex gap-1 overflow-x-auto border-b border-divider/20"
      role="tablist"
      aria-label="프로필 메뉴"
    >
      <button
        v-for="tab in tabs"
        :key="tab.id"
        type="button"
        role="tab"
        :aria-label="`${tab.label} 탭`"
        :aria-selected="activeTab === tab.id"
        class="shrink-0 border-b-2 px-4 py-3 text-sm font-semibold"
        :class="
          activeTab === tab.id
            ? 'border-primary-600 text-primary-700'
            : 'border-transparent text-body-light'
        "
        @click="activeTab = tab.id"
      >
        {{ tab.label }}
        <span
          v-if="tab.count"
          class="ml-1 rounded-full bg-primary-50 px-2 py-0.5 text-xs text-primary-700"
          >{{ tab.count }}</span
        >
      </button>
    </div>

    <div v-if="activeTab === 'projects'" class="mt-6 grid gap-6 lg:grid-cols-[1fr_320px]">
      <main>
        <div class="mb-4 flex items-center justify-between">
          <h2 class="font-headline text-xl font-extrabold">
            내 등록 프로젝트 <span class="text-sm text-primary-700">총 3건</span>
          </h2>
          <div class="flex gap-2">
            <button class="rounded-lg border border-divider/20 px-3 py-2 text-xs">필터</button
            ><button class="rounded-lg border border-divider/20 px-3 py-2 text-xs">
              최신 등록순⌄
            </button>
          </div>
        </div>
        <div class="space-y-4">
          <article
            v-for="project in projects"
            :key="project.id"
            class="rounded-2xl border border-divider/20 bg-white p-5 shadow-sm"
          >
            <div class="flex gap-4">
              <span
                class="grid h-12 w-12 shrink-0 place-items-center rounded-xl bg-primary-50 text-xl font-bold text-primary-700"
                >{{ project.icon }}</span
              >
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center gap-2">
                  <RouterLink
                    :to="`/projects/${project.id}`"
                    class="font-headline text-lg font-extrabold hover:text-primary-600"
                    >{{ project.title }}</RouterLink
                  ><span
                    class="rounded-full bg-emerald-50 px-2 py-1 text-[10px] font-bold text-emerald-700"
                    >● {{ project.status }}</span
                  ><span
                    class="rounded-full bg-amber-50 px-2 py-1 text-[10px] font-bold text-amber-700"
                    >{{ project.badge }}</span
                  >
                </div>
                <p class="mt-3 text-sm leading-6 text-body-light">{{ project.description }}</p>
                <div class="mt-3 flex flex-wrap gap-2">
                  <span
                    v-for="tag in project.tags"
                    :key="tag"
                    class="rounded bg-neutral-100 px-2 py-1 text-[11px] text-body-light"
                    >{{ tag }}</span
                  >
                </div>
              </div>
              <button class="h-8 w-8 text-body-light">•••</button>
            </div>
            <div
              class="mt-5 grid gap-3 rounded-xl bg-neutral-50 p-4 text-xs sm:grid-cols-[repeat(3,1fr)_150px]"
            >
              <span v-for="metric in project.metrics" :key="metric" class="text-body-light">{{
                metric
              }}</span
              ><strong class="text-right text-primary-700">{{ project.growth }} ↗</strong>
            </div>
            <div class="mt-4 flex flex-wrap gap-2">
              <button class="rounded-lg bg-primary-600 px-4 py-2 text-xs font-bold text-white">
                성과 / 통계 상세 대시보드</button
              ><button class="rounded-lg border border-divider/20 px-4 py-2 text-xs font-semibold">
                정보 수정</button
              ><a
                href="#"
                class="rounded-lg border border-divider/20 px-4 py-2 text-xs font-semibold"
                >사이트 바로가기 ↗</a
              >
            </div>
          </article>
        </div>
      </main>

      <aside class="space-y-4">
        <section class="rounded-2xl bg-gradient-to-br from-[#171330] to-primary-700 p-6 text-white">
          <span class="text-2xl">🚀</span>
          <h2 class="mt-4 font-headline text-lg font-extrabold">인디 메이커 런칭 부스트 안내</h2>
          <p class="mt-3 text-sm leading-6 text-blue-100">
            신규 등록 프로젝트는 48시간 동안 Click-Hub 메인과 카테고리 상단에 자동 노출됩니다.
          </p>
          <a href="#" class="mt-5 inline-block text-xs font-bold text-blue-200"
            >48시간 부스트 정책 확인 →</a
          >
        </section>
        <section class="rounded-2xl border border-divider/20 bg-white p-5">
          <div class="flex items-center justify-between">
            <h2 class="font-bold">최근 받은 유저 피드백</h2>
            <a href="#" class="text-xs text-primary-600">전체 보기</a>
          </div>
          <ul class="mt-4 space-y-4">
            <li
              v-for="feedback in [
                ['W', '정우현', 'DevFlow의 실시간 대시보드가 정말 빠르고 직관적이에요.'],
                ['C', 'Chloe Lee', 'PromptCraft의 팀 공유 기능을 기대하고 있어요.'],
              ]"
              :key="feedback[1]"
              class="flex gap-3 text-xs"
            >
              <span
                class="grid h-8 w-8 shrink-0 place-items-center rounded-full bg-secondary text-white"
                >{{ feedback[0] }}</span
              >
              <p>
                <strong>{{ feedback[1] }}</strong
                ><span class="mt-1 block leading-5 text-body-light">{{ feedback[2] }}</span>
              </p>
            </li>
          </ul>
        </section>
        <section class="rounded-2xl border border-divider/20 bg-white p-5">
          <h2 class="font-bold">마이 퀵 메뉴</h2>
          <ul class="mt-4 space-y-3 text-sm text-body-light">
            <li class="flex justify-between">
              <span>☆ 즐겨찾기 프로젝트</span><strong>43</strong>
            </li>
            <li class="flex justify-between"><span>♙ 내 팔로잉 메이커</span><strong>18</strong></li>
            <li class="flex justify-between">
              <span>▤ 내가 작성한 커뮤니티 글</span><strong>16</strong>
            </li>
            <li class="flex justify-between">
              <span>⇩ 월간 성과 리포트 다운로드</span><strong class="text-red-500">PDF</strong>
            </li>
          </ul>
        </section>
      </aside>
    </div>

    <section
      v-else-if="activeTab === 'activity'"
      class="mt-6 rounded-2xl border border-divider/20 bg-white p-6"
    >
      <h2 class="font-headline text-xl font-extrabold">최근 메이커 활동</h2>
      <ol class="mt-5 space-y-4">
        <li
          v-for="item in activity"
          :key="item[0]"
          class="grid gap-2 border-b border-divider/15 pb-4 text-sm sm:grid-cols-[120px_1fr]"
        >
          <time class="text-body-light">{{ item[0] }}</time>
          <p>{{ item[1] }}</p>
        </li>
      </ol>
    </section>
    <section v-else class="mt-6 rounded-2xl border border-divider/20 bg-white p-10 text-center">
      <h2 class="font-headline text-xl font-extrabold">
        {{ tabs.find((tab) => tab.id === activeTab)?.label }}
      </h2>
      <p class="mt-3 text-sm text-body-light">선택한 설정 화면을 준비 중입니다.</p>
    </section>
  </section>
</template>
