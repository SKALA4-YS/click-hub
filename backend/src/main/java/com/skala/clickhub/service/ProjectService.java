package com.skala.clickhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skala.clickhub.dto.project.ProjectDtos.AdminPendingItem;
import com.skala.clickhub.dto.project.ProjectDtos.CreateRequest;
import com.skala.clickhub.dto.project.ProjectDtos.CreateResponse;
import com.skala.clickhub.dto.project.ProjectDtos.DetailResponse;
import com.skala.clickhub.dto.project.ProjectDtos.OutboundClickResponse;
import com.skala.clickhub.dto.project.ProjectDtos.ScreenshotItem;
import com.skala.clickhub.dto.project.ProjectDtos.StatusResponse;
import com.skala.clickhub.dto.project.ProjectDtos.TechStackItem;
import com.skala.clickhub.dto.project.ProjectDtos.TechStackSelection;
import com.skala.clickhub.dto.project.ProjectDtos.UpdateRequest;
import com.skala.clickhub.entity.Category;
import com.skala.clickhub.entity.EventType;
import com.skala.clickhub.entity.PricingType;
import com.skala.clickhub.entity.Project;
import com.skala.clickhub.entity.ProjectStatus;
import com.skala.clickhub.entity.ProjectTechnology;
import com.skala.clickhub.entity.ReactionType;
import com.skala.clickhub.entity.TechGroup;
import com.skala.clickhub.entity.Technology;
import com.skala.clickhub.entity.User;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.CategoryRepository;
import com.skala.clickhub.repository.ProjectReactionRepository;
import com.skala.clickhub.repository.ProjectRepository;
import com.skala.clickhub.repository.ProjectTechnologyRepository;
import com.skala.clickhub.repository.TechnologyRepository;
import com.skala.clickhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    // ObjectMapper 빈 주입 대신 전용 인스턴스 (InteractionEventRecorder와 동일한 이유).
    // screenshots는 {url, alt} 레코드 배열이라 기본 설정으로 충분하다.
    private static final ObjectMapper JSON = new ObjectMapper();

    // 승인 직전 서버가 site_url을 재검증할 때 쓰는 전용 클라이언트. 응답 본문은 필요 없어 discarding으로 받는다.
    private static final Duration URL_CHECK_TIMEOUT = Duration.ofSeconds(5);
    private static final HttpClient URL_CHECK_CLIENT = HttpClient.newBuilder()
            .connectTimeout(URL_CHECK_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final ProjectRepository projectRepository;
    private final ProjectTechnologyRepository projectTechnologyRepository;
    private final ProjectReactionRepository projectReactionRepository;
    private final CategoryRepository categoryRepository;
    private final TechnologyRepository technologyRepository;
    private final UserRepository userRepository;
    private final InteractionEventRecorder interactionEventRecorder;
    private final CategoryThumbnailProvider categoryThumbnailProvider;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 프로젝트 등록. schema.sql의 validate_project_write 트리거가 신규 행을 DRAFT로만 허용하므로
     * 항상 DRAFT로 만들고, 게시는 별도의 submit 요청으로 처리한다.
     */
    @Transactional
    public CreateResponse create(UUID ownerId, CreateRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Category category = resolveCategory(request.categorySlug());

        Project project = Project.builder()
                .owner(owner)
                .primaryCategory(category)
                .title(request.title())
                .description(request.description())
                .siteUrl(request.siteUrl())
                .repositoryUrl(request.repositoryUrl())
                .pricing(parsePricing(request.pricing()))
                .tags(toTagArray(request.tags()))
                .thumbnailUrl(categoryThumbnailProvider.resolve(
                        request.thumbnailUrl(), category == null ? null : category.getSlug()))
                .screenshots(toScreenshotJson(request.screenshots()))
                .status(ProjectStatus.DRAFT)
                .build();

        Project saved = projectRepository.save(project);
        replaceTechStacks(saved, request.techStacks());

        interactionEventRecorder.record(EventType.PROJECT_REGISTERED, saved, ownerId);

        return new CreateResponse(saved.getId(), saved.getStatus().name());
    }

    @Transactional
    public StatusResponse update(UUID projectId, UUID requesterId, UpdateRequest request) {
        Project project = findOwned(projectId, requesterId);

        project.update(
                resolveCategory(request.categorySlug()),
                request.title(),
                request.description(),
                request.siteUrl(),
                request.repositoryUrl(),
                parsePricing(request.pricing()),
                toTagArray(request.tags()),
                request.thumbnailUrl(),
                toScreenshotJson(request.screenshots())
        );
        replaceTechStacks(project, request.techStacks());

        return new StatusResponse(project.getId(), project.getStatus().name());
    }

    /** 게시 요청 (DRAFT → PENDING_REVIEW). */
    @Transactional
    public StatusResponse submitForReview(UUID projectId, UUID requesterId) {
        Project project = findOwned(projectId, requesterId);

        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_STATE);
        }
        project.submitForReview();

        return new StatusResponse(project.getId(), project.getStatus().name());
    }

    /** 인증: ADMIN — 승인 대기(PENDING_REVIEW) 목록. */
    @Transactional(readOnly = true)
    public List<AdminPendingItem> listPendingReview() {
        return projectRepository.findAllByStatus(ProjectStatus.PENDING_REVIEW).stream()
                .map(project -> new AdminPendingItem(
                        project.getId(),
                        project.getTitle(),
                        project.getDescription(),
                        project.getSiteUrl(),
                        project.getStatus().name(),
                        project.getOwner().getDisplayName(),
                        project.getCreatedAt()))
                .toList();
    }

    /** 인증: ADMIN — 소유자가 아니어도 검토를 위해 상세를 열람할 수 있다(등록 폼과 동일한 필드셋). */
    @Transactional(readOnly = true)
    public DetailResponse getDetailForAdmin(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        return toDetail(project, null);
    }

    /**
     * 인증: ADMIN — 승인 (PENDING_REVIEW → PUBLISHED).
     * schema.sql 트리거가 "최근 7일 이내 URL 검증 성공"을 요구하므로, 승인 시점에 서버가 직접
     * site_url을 재검증하고 record_project_url_validation()으로 기록한 뒤 상태를 바꾼다.
     */
    @Transactional
    public StatusResponse approve(UUID projectId) {
        Project project = findPendingReview(projectId);
        if (project.getPrimaryCategory() == null) {
            throw new BusinessException(ErrorCode.PROJECT_CATEGORY_REQUIRED);
        }

        UrlCheckResult check = checkUrl(project.getSiteUrl());
        if (!check.reachable()) {
            throw new BusinessException(ErrorCode.PROJECT_URL_UNREACHABLE);
        }

        // reachable=true 경로만 타므로 httpStatus/finalUrl은 항상 채워져 있다(checkUrl 참고).
        // SQL 함수 호출은 기존 통합 테스트와 동일한 방식(JdbcTemplate)을 그대로 쓴다 — Spring Data
        // 리포지토리로 감싸면 void 반환 함수라 executeUpdate가 실패한다. Project에 걸어둔
        // @DynamicUpdate 덕분에 뒤이은 project.approve() flush는 status 컬럼만 SET하므로,
        // 이 네이티브 호출이 막 갱신한 url_checked_at 등을 stale 엔티티 값으로 덮어쓰지 않는다.
        jdbcTemplate.queryForObject(
                "SELECT record_project_url_validation(?, ?, true, ?, ?)",
                Object.class, projectId, project.getSiteUrl(), check.httpStatus(), check.finalUrl());

        project.approve();
        return new StatusResponse(project.getId(), project.getStatus().name());
    }

    /** 인증: ADMIN — 거절. schema.sql 제약상 사유가 필요하다. */
    @Transactional
    public StatusResponse reject(UUID projectId, String reason) {
        Project project = findPendingReview(projectId);
        project.reject(reason);
        return new StatusResponse(project.getId(), project.getStatus().name());
    }

    /**
     * 프로젝트 삭제.
     * projects 테이블에는 deleted_at이 없어 물리 삭제로 처리한다 — 연관된 반응/댓글/집계/검색문서는
     * ON DELETE CASCADE로 함께 사라지고, interaction_events/notifications의 project_id만
     * ON DELETE SET NULL로 남는다. 이력 보존이 필요하면 스키마에 소프트 삭제 컬럼 추가가 선행되어야 한다.
     */
    @Transactional
    public void delete(UUID projectId, UUID requesterId) {
        Project project = findOwned(projectId, requesterId);
        projectTechnologyRepository.deleteByProjectId(project.getId());
        projectRepository.delete(project);
    }

    /** 인증 선택 — viewerId가 null이면 개인화 필드(likedByMe/favoritedByMe)는 false로 내려간다. */
    @Transactional
    public DetailResponse getDetail(UUID projectId, UUID viewerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        if (project.getStatus() != ProjectStatus.PUBLISHED && !project.isOwnedBy(viewerId)) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }

        interactionEventRecorder.record(EventType.PROJECT_DETAIL_VIEW, project, viewerId);

        return toDetail(project, viewerId);
    }

    @Transactional
    public OutboundClickResponse recordOutboundClick(UUID projectId, UUID viewerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        interactionEventRecorder.record(EventType.OUTBOUND_CLICK, project, viewerId);

        return new OutboundClickResponse(true);
    }

    // --- 내부 헬퍼 ---

    private Project findOwned(UUID projectId, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        if (!project.isOwnedBy(requesterId)) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_OWNER);
        }
        return project;
    }

    private Project findPendingReview(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        if (project.getStatus() != ProjectStatus.PENDING_REVIEW) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_STATE);
        }
        return project;
    }

    private record UrlCheckResult(boolean reachable, Integer httpStatus, String finalUrl) {}

    /** 응답 본문은 필요 없어 discarding으로 받고, 2xx~3xx만 "접속 가능"으로 취급한다. */
    private UrlCheckResult checkUrl(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(URL_CHECK_TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<Void> response = URL_CHECK_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            boolean reachable = response.statusCode() >= 200 && response.statusCode() < 400;
            return new UrlCheckResult(reachable, response.statusCode(), response.uri().toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new UrlCheckResult(false, null, null);
        } catch (Exception e) {
            return new UrlCheckResult(false, null, null);
        }
    }

    private Category resolveCategory(String categorySlug) {
        if (categorySlug == null || categorySlug.isBlank()) {
            return null;
        }
        return categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private PricingType parsePricing(String pricing) {
        if (pricing == null || pricing.isBlank()) {
            return PricingType.UNKNOWN;
        }
        try {
            return PricingType.valueOf(pricing.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String[] toTagArray(List<String> tags) {
        if (tags == null) {
            return new String[0];
        }
        return tags.stream().filter(t -> t != null && !t.isBlank()).distinct().toArray(String[]::new);
    }

    private JsonNode toScreenshotJson(List<ScreenshotItem> screenshots) {
        return JSON.valueToTree(screenshots == null ? List.of() : screenshots);
    }

    private void replaceTechStacks(Project project, List<TechStackSelection> selections) {
        projectTechnologyRepository.deleteByProjectId(project.getId());
        if (selections == null || selections.isEmpty()) {
            return;
        }

        List<ProjectTechnology> rows = new ArrayList<>();
        for (TechStackSelection selection : selections) {
            Technology technology = technologyRepository.findBySlug(selection.technologySlug())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TECHNOLOGY_NOT_FOUND));

            rows.add(ProjectTechnology.builder()
                    .project(project)
                    .technology(technology)
                    .technologyGroup(resolveGroup(selection.group(), technology))
                    .version(selection.version())
                    .build());
        }
        projectTechnologyRepository.saveAll(rows);
    }

    private TechGroup resolveGroup(String group, Technology technology) {
        if (group == null || group.isBlank()) {
            return technology.getDefaultGroup();
        }
        try {
            return TechGroup.valueOf(group.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private DetailResponse toDetail(Project project, UUID viewerId) {
        List<TechStackItem> techStacks = projectTechnologyRepository.findByProjectId(project.getId()).stream()
                .map(pt -> new TechStackItem(
                        pt.getTechnology().getName(),
                        pt.getTechnology().getSlug(),
                        pt.getId().getTechnologyGroup().name(),
                        pt.getVersion()))
                .toList();

        long likeCount = projectReactionRepository.countByIdProjectIdAndIdType(project.getId(), ReactionType.LIKE);
        long favoriteCount = projectReactionRepository.countByIdProjectIdAndIdType(project.getId(), ReactionType.FAVORITE);

        boolean likedByMe = viewerId != null && projectReactionRepository
                .existsByIdUserIdAndIdProjectIdAndIdType(viewerId, project.getId(), ReactionType.LIKE);
        boolean favoritedByMe = viewerId != null && projectReactionRepository
                .existsByIdUserIdAndIdProjectIdAndIdType(viewerId, project.getId(), ReactionType.FAVORITE);

        return new DetailResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getSiteUrl(),
                project.getRepositoryUrl(),
                project.getPricing().name(),
                project.getStatus().name(),
                project.getPrimaryCategory() == null ? null : project.getPrimaryCategory().getSlug(),
                project.getPrimaryCategory() == null ? null : project.getPrimaryCategory().getName(),
                List.of(project.getTags()),
                project.getThumbnailUrl(),
                readScreenshots(project.getScreenshots()),
                techStacks,
                project.getOwner().getDisplayName(),
                project.getOwner().getId(),
                project.getPublishedAt(),
                likeCount,
                favoriteCount,
                likedByMe,
                favoritedByMe
        );
    }

    private List<ScreenshotItem> readScreenshots(JsonNode screenshots) {
        if (screenshots == null || !screenshots.isArray()) {
            return List.of();
        }
        List<ScreenshotItem> items = new ArrayList<>();
        screenshots.forEach(node -> items.add(new ScreenshotItem(
                node.path("url").asText(null),
                node.path("alt").asText(null))));
        return items;
    }
}
