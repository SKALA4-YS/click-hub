// 프로젝트 등록 폼에서 쓰는 고정 목록 — schema.sql seed 데이터와 동일한 slug를 쓴다.
// (category/technology는 실제 API에서도 slug로 지정하고 백엔드가 slug -> id를 resolve한다.)

export const categoryOptions = [
  { slug: 'productivity-work', name: '생산성/업무' },
  { slug: 'education-career', name: '교육/취업' },
  { slug: 'developer-tools', name: '개발자 도구' },
  { slug: 'finance', name: '금융' },
  { slug: 'life-health', name: '생활/건강' },
  { slug: 'content-entertainment', name: '콘텐츠/엔터테인먼트' },
  { slug: 'social-community', name: '소셜/커뮤니티' },
  { slug: 'shopping-commerce', name: '쇼핑/커머스' },
  { slug: 'travel-local', name: '여행/지역' },
  { slug: 'design-creative', name: '디자인/크리에이티브' },
  { slug: 'ai-service', name: 'AI 서비스' },
  { slug: 'data-analytics', name: '데이터/분석' },
  { slug: 'security-auth', name: '보안/인증' },
  { slug: 'other', name: '기타' },
]

export const pricingOptions = [
  { value: 'FREE', label: '무료' },
  { value: 'FREEMIUM', label: '프리미엄(부분 유료)' },
  { value: 'PAID', label: '유료' },
  { value: 'UNKNOWN', label: '미정' },
]

export const techGroupLabels = {
  FRONTEND: 'Frontend',
  BACKEND: 'Backend',
  DATABASE: 'Database',
  INFRA_DEPLOY: 'Infra/Deploy',
  AI_DATA: 'AI/Data',
}

export const technologyCatalog = [
  { slug: 'vue-js', name: 'Vue.js', group: 'FRONTEND' },
  { slug: 'react', name: 'React', group: 'FRONTEND' },
  { slug: 'typescript', name: 'TypeScript', group: 'FRONTEND' },
  { slug: 'spring-boot', name: 'Spring Boot', group: 'BACKEND' },
  { slug: 'node-js', name: 'Node.js', group: 'BACKEND' },
  { slug: 'fastapi', name: 'FastAPI', group: 'BACKEND' },
  { slug: 'postgresql', name: 'PostgreSQL', group: 'DATABASE' },
  { slug: 'redis', name: 'Redis', group: 'DATABASE' },
  { slug: 'mongodb', name: 'MongoDB', group: 'DATABASE' },
  { slug: 'docker', name: 'Docker', group: 'INFRA_DEPLOY' },
  { slug: 'aws', name: 'AWS', group: 'INFRA_DEPLOY' },
  { slug: 'vercel', name: 'Vercel', group: 'INFRA_DEPLOY' },
  { slug: 'github-actions', name: 'GitHub Actions', group: 'INFRA_DEPLOY' },
  { slug: 'openai-api', name: 'OpenAI API', group: 'AI_DATA' },
  { slug: 'pgvector', name: 'pgvector', group: 'AI_DATA' },
]
