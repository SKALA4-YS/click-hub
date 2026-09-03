package com.skala.clickhub.service;

import com.skala.clickhub.common.response.CursorCodec;
import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.favorite.FavoriteDtos.FavoriteResponse;
import com.skala.clickhub.dto.project.ProjectDtos.SummaryResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentCreateRequest;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.LikeResponse;
import com.skala.clickhub.entity.EventType;
import com.skala.clickhub.entity.Project;
import com.skala.clickhub.entity.ProjectComment;
import com.skala.clickhub.entity.ProjectReaction;
import com.skala.clickhub.entity.ReactionType;
import com.skala.clickhub.entity.User;
import com.skala.clickhub.entity.id.ProjectReactionId;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.ProjectCommentRepository;
import com.skala.clickhub.repository.ProjectReactionRepository;
import com.skala.clickhub.repository.ProjectRepository;
import com.skala.clickhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * §5 — 좋아요/즐겨찾기 토글, 내 즐겨찾기 목록, 프로젝트 댓글. CommunityService처럼 한 기능
 * 그룹(리액션+댓글)을 한 서비스에 묶는다.
 */
@Service
@RequiredArgsConstructor
public class ReactionService {

    private static final int PAGE_SIZE = 20;

    private final ProjectRepository projectRepository;
    private final ProjectReactionRepository projectReactionRepository;
    private final ProjectCommentRepository projectCommentRepository;
    private final UserRepository userRepository;
    private final InteractionEventRecorder interactionEventRecorder;

    @Transactional
    public LikeResponse toggleLike(UUID projectId, UUID userId) {
        Project project = findProject(projectId);
        boolean nowLiked = toggleReaction(project, userId, ReactionType.LIKE);
        interactionEventRecorder.record(EventType.LIKE_SET, project, userId);

        long likeCount = projectReactionRepository.countByIdProjectIdAndIdType(projectId, ReactionType.LIKE);
        return new LikeResponse(nowLiked, likeCount);
    }

    @Transactional
    public FavoriteResponse toggleFavorite(UUID projectId, UUID userId) {
        Project project = findProject(projectId);
        boolean nowFavorited = toggleReaction(project, userId, ReactionType.FAVORITE);
        interactionEventRecorder.record(EventType.FAVORITE_SET, project, userId);

        return new FavoriteResponse(nowFavorited);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<SummaryResponse> listFavorites(UUID userId, String cursor) {
        int offset = CursorCodec.decode(cursor);

        List<Project> rows = projectRepository.findFavoritedByUser(userId, PAGE_SIZE + 1, offset);

        boolean hasNext = rows.size() > PAGE_SIZE;
        List<SummaryResponse> items = rows.stream()
                .limit(PAGE_SIZE)
                .map(this::toSummary)
                .toList();

        return CursorPageResponse.of(items, hasNext ? CursorCodec.encode(offset + PAGE_SIZE) : null);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentResponse> listComments(UUID projectId, String cursor) {
        findProject(projectId);
        int offset = CursorCodec.decode(cursor);

        List<ProjectComment> rows = projectCommentRepository.findActiveByProject(projectId, PAGE_SIZE + 1, offset);

        boolean hasNext = rows.size() > PAGE_SIZE;
        List<CommentResponse> items = rows.stream()
                .limit(PAGE_SIZE)
                .map(this::toCommentResponse)
                .toList();

        return CursorPageResponse.of(items, hasNext ? CursorCodec.encode(offset + PAGE_SIZE) : null);
    }

    @Transactional
    public CommentResponse createComment(UUID projectId, UUID authorId, CommentCreateRequest request) {
        Project project = findProject(projectId);
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ProjectComment comment = projectCommentRepository.save(ProjectComment.builder()
                .project(project)
                .author(author)
                .body(request.body())
                .build());

        interactionEventRecorder.record(EventType.COMMENT_CREATED, project, authorId);

        return toCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(UUID projectId, UUID commentId, UUID requesterId) {
        ProjectComment comment = projectCommentRepository.findActiveById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_COMMENT_NOT_FOUND));

        if (!comment.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.PROJECT_COMMENT_NOT_FOUND);
        }
        if (!comment.getAuthor().getId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.NOT_COMMENT_AUTHOR);
        }
        comment.softDelete();
    }

    // --- 내부 헬퍼 ---

    private Project findProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    /** 반응이 있으면 취소(삭제), 없으면 생성 — 반환값은 토글 후 "지금 활성 상태인지". */
    private boolean toggleReaction(Project project, UUID userId, ReactionType type) {
        ProjectReactionId id = new ProjectReactionId(userId, project.getId(), type);

        if (projectReactionRepository.existsById(id)) {
            projectReactionRepository.deleteById(id);
            return false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        projectReactionRepository.save(ProjectReaction.builder()
                .user(user)
                .project(project)
                .type(type)
                .build());
        return true;
    }

    private SummaryResponse toSummary(Project project) {
        return new SummaryResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getThumbnailUrl(),
                project.getPrimaryCategory() == null ? null : project.getPrimaryCategory().getSlug(),
                project.getPrimaryCategory() == null ? null : project.getPrimaryCategory().getName(),
                project.getPricing().name(),
                List.of(project.getTags()),
                project.getOwner().getDisplayName(),
                project.getPublishedAt()
        );
    }

    private CommentResponse toCommentResponse(ProjectComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthor().getDisplayName(),
                comment.getBody(),
                comment.getCreatedAt()
        );
    }
}
