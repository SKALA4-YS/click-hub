import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/rankings',
      name: 'rankings',
      component: () => import('../views/RankingView.vue'),
    },
    {
      path: '/projects/:id',
      name: 'project-detail',
      component: () => import('../views/ProjectDetailView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { standalone: true },
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('../views/SignupView.vue'),
    },
    {
      path: '/onboarding',
      name: 'onboarding',
      component: () => import('../views/OnboardingView.vue'),
      meta: { standalone: true },
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('../views/SignupView.vue'),
      meta: { standalone: true },
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('../views/MyPageView.vue'),
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('../views/FavoritesView.vue'),
    },
    {
      path: '/following',
      name: 'following',
      component: () => import('../views/FollowingView.vue'),
    },
    {
      path: '/notifications',
      name: 'notifications',
      component: () => import('../views/NotificationsView.vue'),
    },
    {
      path: '/projects/new',
      name: 'project-register',
      component: () => import('../views/ProjectRegisterView.vue'),
    },
    {
      path: '/projects/:id',
      name: 'project-detail',
      component: () => import('../views/ProjectDetailView.vue'),
    },
    {
      path: '/rankings',
      name: 'rankings',
      component: () => import('../views/ProjectListView.vue'),
    },
    {
      path: '/rankings/developers',
      name: 'developer-rankings',
      component: () => import('../views/DeveloperRankingView.vue'),
    },
    {
      path: '/developers/:id',
      name: 'developer-detail',
      component: () => import('../views/DeveloperDetailView.vue'),
    },
    {
      path: '/community',
      name: 'community',
      component: () => import('../views/CommunityBoardView.vue'),
    },
    {
      path: '/tutorials',
      name: 'tutorials',
      component: () => import('../views/TutorialsView.vue'),
    },
    {
      path: '/insights',
      name: 'insights',
      component: () => import('../views/InsightsView.vue'),
    },
  ],
})

export default router
