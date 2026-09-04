package com.skala.clickhub.service;

import com.skala.clickhub.common.response.CursorCodec;
import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.feed.FeedDtos.FeedItem;
import com.skala.clickhub.entity.Project;
import com.skala.clickhub.entity.ReactionType;
import com.skala.clickhub.repository.ProjectReactionRepository;
import com.skala.clickhub.repository.ProjectReactionRepository.ProjectReactionCount;
import com.skala.clickhub.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 홈 종합 피드 (기획서 4장).
 *
 * 4장은 아직 팀 리뷰 중이라 화면 구성이 확정되지 않았다. 여기서는 확정된 부분(= 인기·최신·다양성을
 * 하나의 피드로 합친다)만 구현하고, 개인화(협업 필터링/최근 행동 반영)는 행동 데이터가 쌓인 뒤
 * ProjectRepository.findHomeFeed의 정렬식에 항을 추가하는 방식으로 확장한다.
 */
@Service
@RequiredArgsConstructor
public class FeedService {

    private static final int PAGE_SIZE = 20;

    private final ProjectRepository projectRepository;
    private final ProjectReactionRepository projectReactionRepository;

    @Transactional(readOnly = true)
    public CursorPageResponse<FeedItem> getHomeFeed(String cursor) {
        int offset = CursorCodec.decode(cursor);

        List<Project> rows = projectRepository.findHomeFeed(PAGE_SIZE + 1, offset);

        boolean hasNext = rows.size() > PAGE_SIZE;
        List<Project> pageRows = rows.stream().limit(PAGE_SIZE).toList();
        Map<UUID, Long> likeCounts = batchLikeCounts(pageRows);

        List<FeedItem> items = pageRows.stream()
                .map(project -> toItem(project, likeCounts.getOrDefault(project.getId(), 0L)))
                .toList();

        return CursorPageResponse.of(items, hasNext ? CursorCodec.encode(offset + PAGE_SIZE) : null);
    }

    /**
     * 프로젝트 하나마다 countByIdProjectIdAndIdType를 부르면 페이지당 N번의 COUNT 쿼리가
     * 나간다(N+1). 현재 페이지의 project_id를 모아 한 번의 GROUP BY로 묶어 가져온다.
     */
    private Map<UUID, Long> batchLikeCounts(List<Project> projects) {
        if (projects.isEmpty()) {
            return Map.of();
        }
        List<UUID> projectIds = projects.stream().map(Project::getId).toList();
        return projectReactionRepository.countGroupedByProjectIds(projectIds, ReactionType.LIKE).stream()
                .collect(Collectors.toMap(ProjectReactionCount::getProjectId, ProjectReactionCount::getCount));
    }

    private FeedItem toItem(Project project, long likeCount) {
        return new FeedItem(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getThumbnailUrl(),
                project.getPrimaryCategory() == null ? null : project.getPrimaryCategory().getSlug(),
                project.getPrimaryCategory() == null ? null : project.getPrimaryCategory().getName(),
                List.of(project.getTags()),
                project.getOwner().getDisplayName(),
                project.getPublishedAt(),
                likeCount
        );
    }
}
