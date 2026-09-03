package com.skala.clickhub.service;

import com.skala.clickhub.common.response.CursorCodec;
import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.search.SearchDtos.SearchResultItem;
import com.skala.clickhub.entity.Project;
import com.skala.clickhub.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 통합 검색 (기획서 5장).
 *
 * 확정 방침(2026-09-03): 검색에는 AI/LLM을 사용하지 않는다. 키워드 + 메타데이터 필터로만 동작하며,
 * pgvector 유사도 결합은 임베딩 파이프라인이 생긴 뒤 ProjectRepository.search에 항을 추가하면 된다.
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int PAGE_SIZE = 20;

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public CursorPageResponse<SearchResultItem> search(String q, String category,
                                                       List<String> tags, List<String> tech,
                                                       String cursor) {
        int offset = CursorCodec.decode(cursor);

        // 다음 페이지 존재 여부를 알기 위해 한 건 더 조회한다.
        List<Project> rows = projectRepository.search(
                nullToEmpty(q),
                nullToEmpty(category),
                toCsv(tags),
                toCsv(tech),
                PAGE_SIZE + 1,
                offset
        );

        boolean hasNext = rows.size() > PAGE_SIZE;
        List<SearchResultItem> items = rows.stream()
                .limit(PAGE_SIZE)
                .map(this::toItem)
                .toList();

        return CursorPageResponse.of(items, hasNext ? CursorCodec.encode(offset + PAGE_SIZE) : null);
    }

    private SearchResultItem toItem(Project project) {
        return new SearchResultItem(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getThumbnailUrl(),
                project.getPrimaryCategory() == null ? null : project.getPrimaryCategory().getSlug(),
                project.getPrimaryCategory() == null ? null : project.getPrimaryCategory().getName(),
                List.of(project.getTags()),
                project.getOwner().getDisplayName(),
                project.getPublishedAt()
        );
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String toCsv(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return String.join(",", values.stream().filter(v -> v != null && !v.isBlank()).toList());
    }
}
