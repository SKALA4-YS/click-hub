export const insightFilters = [
  { id: 'all', label: '전체 트렌드' },
  { id: 'agent', label: '급상승 검색어' },
  { id: 'builder', label: '빌더 채택 기술' },
  { id: 'data', label: '신규 런칭 인기 테마' },
  { id: 'global', label: '해외·PH 급상승' },
]

export const rankedTrends = [
  {
    id: 'local-llms',
    rank: 1,
    category: 'agent',
    tags: ['#AgenticWorkflow', '#OnDevice'],
    growth: '+185%',
    saves: '24개',
    title: 'Local LLMs On-Device AI 경량화 에이전트',
    summary:
      'Ollama, WebLLM, Llama.cpp를 프론트엔드/데스크톱 앱에 결합해 서버 API 호출 비용 없이 브라우저에서 즉시 동작하는 로컬 에이전트 패턴이 급부상하고 있습니다.',
    stack: '핵심 툴: Ollama / WebLLM / Llama.cpp / Transformers.js',
    point: 'B2B 채택 1위',
    to: '/projects/prj_301',
  },
  {
    id: 'voice-agent',
    rank: 2,
    category: 'agent',
    tags: ['#WebRTC', '#VoiceAI'],
    growth: '+142%',
    saves: '19개',
    title: 'AI Voice Agent & WebRTC 초저지연 실시간 음성 인터랙션',
    summary:
      'OpenAI Realtime API 및 Cartesia, ElevenLabs를 활용해 레이턴시 300ms 미만의 자연스러운 양방향 음성 상담 봇, 외국어 회화, 튜터, 인바운드 세일즈 봇이 다수 런칭되었습니다.',
    stack: '핵심 툴: Realtime API / WebRTC / LiveKit',
    point: '음성 AI 실전 도입 급증',
    to: '/projects/prj_302',
  },
  {
    id: 'canvas-builder',
    rank: 3,
    category: 'builder',
    tags: ['#CanvasUI', '#InfiniteCanvas'],
    growth: '+118%',
    saves: '15개',
    title: 'Canvas-based UI Builder & 무한 캔버스 생성형 작업도구',
    summary:
      '채팅창을 벗어나 Tldraw, Excalidraw 기반의 무한 화이트보드 공간에서 노드와 카드를 연결하며 코드를 생성하고 시각화하는 노코드·로우코드 빌더 테마가 큰 관심을 얻고 있습니다.',
    stack: '핵심 툴: Tldraw SDK / React Flow',
    point: 'UX 혁신 부문 1위',
    to: '/projects/prj_303',
  },
  {
    id: 'multimodal-rag',
    rank: 4,
    category: 'data',
    tags: ['#MultimodalRAG', '#HybridSearch'],
    growth: '+94%',
    saves: '31개',
    title: 'Multimodal RAG Pipeline & 복합 문서 분석 시스템',
    summary:
      '단순 텍스트 검색을 넘어 복잡한 표, 차트, PDF 스캔본을 구조화하여 정확도를 98%까지 끌어올린 RAG 솔루션으로 공공·금융·법률 문서 분석 도구 채택이 두드러집니다.',
    stack: '핵심 툴: Supabase pgvector / Cohere Rerank',
    point: '수익화 성공률 최상위',
    to: '/projects/prj_301',
  },
]

export const hotKeywords = [
  'DeepSeek R1 로컬 연동',
  'Cursor 커스텀 룰 (rules)',
  'v0 템플릿 복제',
  'Supabase 벡터 검색',
  '인디해커 Stripe 정산',
  '외국어 코칭 자동화',
  'Next.js 15 Server Action',
  'Claude 3.5 Sonnet 프롬프트',
]

export const makerOpportunities = [
  {
    id: 'vertical-agent',
    marker: 'A',
    title: '버티컬 AI 마이크로 에이전트',
    metric: '유료 전환율 3.8배',
    summary:
      '법률·특허·이커머스 CS·부동산 실사 등 뾰족한 산업별 특화 에이전트의 유료 구독 전환율이 일반 SaaS 대비 높게 집계되었습니다.',
  },
  {
    id: 'browser-overlay',
    marker: 'B',
    title: 'Chrome 확장 프로그램 + AI 오버레이',
    metric: '리텐션 급상승',
    summary:
      '별도 웹 대시보드 접속 없이 유저의 기존 브라우징 동선에 즉시 개입하는 플러그인형 도구의 30일 리텐션이 높습니다.',
  },
  {
    id: 'devtools',
    marker: 'C',
    title: '개발자 도구 (DevTools) 자동화',
    metric: '메이커 수요 폭발',
    summary:
      'PR 리뷰, 오류 로그, 테스트 분석에 AI가 직접 개입하도록 하는 개발자용 보조 도구가 빠르게 늘고 있습니다.',
  },
]

export const emergingStacks = [
  {
    id: 'vercel-ai-sdk',
    title: 'Vercel AI SDK 4.0',
    metric: '점유율 48%',
    summary:
      'useChat, useCompletion을 통한 실시간 스트리밍 챗봇 및 LLM Proxy 패턴이 가장 빠르게 확산 중입니다.',
    tags: ['#Next.js', '#AI SDK'],
  },
  {
    id: 'langgraph',
    title: 'LangChain / LangGraph',
    metric: '공유량 29%',
    summary: '상태를 쉽게 제어하는 Graph 흐름을 에이전트 간 협업 및 장기 메모리 구현에 사용합니다.',
    tags: ['#Agentic'],
  },
  {
    id: 'supabase-pgvector',
    title: 'Supabase pgvector',
    metric: 'Supabase 34%',
    summary:
      '벡터 검색과 인증을 한 번에 연결해 빠르게 RAG 프로토타입을 만드는 선택이 늘고 있습니다.',
    tags: ['#RAG', '#Database'],
  },
  {
    id: 'deepseek-coder',
    title: 'DeepSeek Coder V2',
    metric: 'DeepSeek 가성비 1위',
    summary: '코드 생성과 리팩터링에 특화된 경량 오픈 모델로 개발 환경 통합 사례가 증가했습니다.',
    tags: ['#OpenSource'],
  },
]
