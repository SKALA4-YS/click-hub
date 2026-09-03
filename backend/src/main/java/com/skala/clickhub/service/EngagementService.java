package com.skala.clickhub.service;

import com.skala.clickhub.dto.favorite.FavoriteDtos.FavoriteResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentCreateRequest;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.LikeResponse;
import com.skala.clickhub.entity.EventType;
import com.skala.clickhub.entity.Project;
import com.skala.clickhub.entity.ProjectComment;
import com.skala.clickhub.entity.ProjectReaction;
import com.skala.clickhub.entity.ProjectStatus;
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

@Service
@RequiredArgsConstructor
public class EngagementService {

    private final ProjectRepository projectRepository;
    private final ProjectReactionRepository projectReactionRepository;
    private final ProjectCommentRepository projectCommentRepository;
    private final UserRepository userRepository;
    private final InteractionEventRecorder interactionEventRecorder;

    @Transactional
    public LikeResponse toggleLike(UUID userId, UUID projectId) {
        Project project = requireProject(projectId);
        User user = requireUser(userId);
        boolean liked = toggleReaction(user, project, ReactionType.LIKE);
        interactionEventRecorder.recordToggle(EventType.LIKE_SET, project, userId, liked);
        long count = projectReactionRepository.countByIdProjectIdAndIdType(projectId, ReactionType.LIKE);
        return new LikeResponse(liked, count);
    }

    @Transactional
    public FavoriteResponse toggleFavorite(UUID userId, UUID projectId) {
        Project project = requireProject(projectId);
        User user = requireUser(userId);
        boolean favorited = toggleReaction(user, project, ReactionType.FAVORITE);
        interactionEventRecorder.recordToggle(EventType.FAVORITE_SET, project, userId, favorited);
        return new FavoriteResponse(favorited);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return projectCommentRepository.findActiveByProjectId(projectId).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional
    public CommentResponse createComment(UUID userId, UUID projectId, CommentCreateRequest request) {
        Project project = requireProject(projectId);
        User author = requireUser(userId);
        ProjectComment comment = projectCommentRepository.save(ProjectComment.builder()
                .project(project)
                .author(author)
                .body(request.body().trim())
                .build());
        interactionEventRecorder.record(EventType.COMMENT_CREATED, project, userId);
        return toCommentResponse(comment);
    }

    private boolean toggleReaction(User user, Project project, ReactionType type) {
        ProjectReactionId id = new ProjectReactionId(user.getId(), project.getId(), type);
        if (projectReactionRepository.existsById(id)) {
            projectReactionRepository.deleteById(id);
            projectReactionRepository.flush();
            return false;
        }
        projectReactionRepository.save(ProjectReaction.builder()
                .user(user)
                .project(project)
                .type(type)
                .build());
        projectReactionRepository.flush();
        return true;
    }

    private Project requireProject(UUID projectId) {
        return projectRepository.findByIdAndStatus(projectId, ProjectStatus.PUBLISHED)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private CommentResponse toCommentResponse(ProjectComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getDisplayName(),
                comment.getBody(),
                comment.getCreatedAt()
        );
    }
}
