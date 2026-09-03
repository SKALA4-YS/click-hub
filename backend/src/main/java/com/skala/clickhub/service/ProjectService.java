package com.skala.clickhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    // ObjectMapper 빈 주입 대신 전용 인스턴스 (InteractionEventRecorder와 동일한 이유).
    // screenshots는 {url, alt} 레코드 배열이라 기본 설정으로 충분하다.
    private static final ObjectMapper JSON = new ObjectMapper();

    private final ProjectRepository projectRepository;
    private final ProjectTechnologyRepository projectTechnologyRepository;
    private final ProjectReactionRepository projectReactionRepository;
    private final CategoryRepository categoryRepository;
    private final TechnologyRepository technologyRepository;
    private final UserRepository userRepository;
    private final InteractionEventRecorder interactionEventRecorder;

    /**
     * 프로젝트 등록. schema.sql의 validate_project_write 트리거가 신규 행을 DRAFT로만 허용하므로
     * 항상 DRAFT로 만들고, 게시는 별도의 submit 요청으로 처리한다.
     */
    @Transactional
    public CreateResponse create(UUID ownerId, CreateRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Project project = Project.builder()
                .owner(owner)
                .primaryCategory(resolveCategory(request.categorySlug()))
                .title(request.title())
                .description(request.description())
                .siteUrl(request.siteUrl())
                .repositoryUrl(request.repositoryUrl())
                .pricing(parsePricing(request.pricing()))
                .tags(toTagArray(request.tags()))
                .thumbnailUrl(request.thumbnailUrl())
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
