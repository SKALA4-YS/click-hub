export function toSiteCardProject(project, overrides = {}) {
  const likeCount = project.likeCount ?? project.stats?.likes ?? 0
  const commentCount = project.commentCount ?? project.stats?.comments ?? 0
  const viewCount = project.viewCount ?? project.stats?.views ?? 0

  return {
    id: project.id ?? project.projectId,
    title: project.title,
    description: project.description ?? '',
    thumbnail_url: project.thumbnailUrl ?? project.thumbnail_url ?? null,
    category: project.categoryName ?? project.category ?? '기타',
    category_slug: project.categorySlug ?? project.category_slug ?? null,
    tags: project.tags ?? [],
    owner_name: project.ownerName ?? project.owner_name ?? '',
    published_at: project.publishedAt ?? project.published_at ?? null,
    status: project.status ?? 'PUBLISHED',
    stats: {
      likes: likeCount,
      comments: commentCount,
      views: viewCount,
    },
    ...overrides,
  }
}

export function toProjectDetailViewModel(project, comments = [], creator = null) {
  const techStack = (project.techStacks ?? []).reduce((groups, item) => {
    const group = item.group || 'OTHER'
    groups[group] ??= []
    groups[group].push(
      item.version ? `${item.technologyName} ${item.version}` : item.technologyName,
    )
    return groups
  }, {})

  return {
    id: project.id,
    title: project.title,
    description: project.description,
    site_url: project.siteUrl,
    repository_url: project.repositoryUrl,
    category: project.categoryName ?? '기타',
    category_slug: project.categorySlug,
    tags: project.tags ?? [],
    thumbnail_url: project.thumbnailUrl,
    created_at: project.publishedAt,
    owner: {
      id: project.ownerId,
      display_name: project.ownerName,
      avatar_initial: project.ownerName?.slice(0, 1) || '?',
      followers: creator?.subscriberCount ?? 0,
      project_count: creator?.projects?.length ?? 0,
    },
    tech_stack: techStack,
    stats: {
      likes: project.likeCount ?? 0,
      favorites: project.favoriteCount ?? 0,
      views: 0,
      comments: comments.length,
    },
    liked_by_me: project.likedByMe ?? false,
    favorited_by_me: project.favoritedByMe ?? false,
    subscribed_by_me: creator?.subscribedByMe ?? false,
    comments: comments.map((comment) => ({
      id: comment.id,
      author: comment.authorName,
      author_id: comment.authorId,
      body: comment.body,
      created_at: comment.createdAt,
    })),
  }
}
