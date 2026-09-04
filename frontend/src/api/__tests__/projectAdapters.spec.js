import { describe, expect, it } from 'vitest'

import { toSiteCardProject } from '@/api/adapters/projects'

describe('project API adapters', () => {
  it('maps the backend camelCase DTO to the existing card view model', () => {
    expect(
      toSiteCardProject({
        id: 'project-id',
        title: 'Click HUB',
        description: 'description',
        thumbnailUrl: 'https://example.com/image.png',
        categorySlug: 'developer-tools',
        categoryName: '개발자 도구',
        ownerName: '메이커',
        publishedAt: '2026-09-04T00:00:00Z',
        likeCount: 3,
      }),
    ).toMatchObject({
      id: 'project-id',
      thumbnail_url: 'https://example.com/image.png',
      category_slug: 'developer-tools',
      category: '개발자 도구',
      owner_name: '메이커',
      stats: { likes: 3, comments: 0, views: 0 },
    })
  })
})
