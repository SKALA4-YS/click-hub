package com.skala.clickhub.service;

import com.skala.clickhub.common.response.CursorCodec;
import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.community.CommunityDtos.BoardResponse;
import com.skala.clickhub.dto.community.CommunityDtos.CommentCreateRequest;
import com.skala.clickhub.dto.community.CommunityDtos.CommentResponse;
import com.skala.clickhub.dto.community.CommunityDtos.PostCreateRequest;
import com.skala.clickhub.dto.community.CommunityDtos.PostCreateResponse;
import com.skala.clickhub.dto.community.CommunityDtos.PostDetailResponse;
import com.skala.clickhub.dto.community.CommunityDtos.PostSummaryResponse;
import com.skala.clickhub.dto.community.CommunityDtos.PostUpdateRequest;
import com.skala.clickhub.entity.CommunityBoard;
import com.skala.clickhub.entity.CommunityPost;
import com.skala.clickhub.entity.CommunityPostComment;
import com.skala.clickhub.entity.User;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.CommunityBoardRepository;
import com.skala.clickhub.repository.CommunityPostCommentRepository;
import com.skala.clickhub.repository.CommunityPostRepository;
import com.skala.clickhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 커뮤니티 게시판 (기획서 3장 포함 항목, V1 스키마 community_* 테이블).
 *
 * 게시판 전체가 로그인 사용자 전용이다(2026-09-03 확정) — 조회도 인증이 필요하므로
 * SecurityConfig에서 GET까지 인증 대상으로 잡아둔다.
 */
@Service
@RequiredArgsConstructor
public class CommunityService {

    private static final int PAGE_SIZE = 20;
    private static final String UNKNOWN_AUTHOR = "알 수 없는 사용자";

    private final CommunityBoardRepository communityBoardRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostCommentRepository communityPostCommentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BoardResponse> getBoards() {
        return communityBoardRepository.findActiveBoards().stream()
                .map(board -> new BoardResponse(
                        board.getId(),
                        board.getSlug(),
                        board.getName(),
                        board.getDescription(),
                        board.getDisplayOrder()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<PostSummaryResponse> getPosts(String boardSlug, String cursor) {
        CommunityBoard board = findBoard(boardSlug);
        int offset = CursorCodec.decode(cursor);

        List<CommunityPost> rows =
                communityPostRepository.findPublishedByBoard(board.getId(), PAGE_SIZE + 1, offset);

        boolean hasNext = rows.size() > PAGE_SIZE;
        List<PostSummaryResponse> items = rows.stream()
                .limit(PAGE_SIZE)
                .map(post -> new PostSummaryResponse(
                        post.getId(),
                        post.getTitle(),
                        authorName(post.getAuthor()),
                        post.getViewCount(),
                        post.getCreatedAt()))
                .toList();

        return CursorPageResponse.of(items, hasNext ? CursorCodec.encode(offset + PAGE_SIZE) : null);
    }

    @Transactional
    public PostCreateResponse createPost(String boardSlug, UUID authorId, PostCreateRequest request) {
        CommunityBoard board = findBoard(boardSlug);
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        CommunityPost post = communityPostRepository.save(CommunityPost.builder()
                .board(board)
                .author(author)
                .title(request.title())
                .body(request.body())
                .build());

        return new PostCreateResponse(post.getId());
    }

    @Transactional
    public PostDetailResponse getPostDetail(UUID postId, UUID viewerId) {
        CommunityPost post = findActivePost(postId);
        communityPostRepository.incrementViewCount(postId);

        return new PostDetailResponse(
                post.getId(),
                post.getBoard().getSlug(),
                post.getTitle(),
                post.getBody(),
                authorName(post.getAuthor()),
                post.getAuthor() == null ? null : post.getAuthor().getId(),
                // 방금 증가시킨 값을 화면에도 즉시 반영한다(UPDATE는 영속성 컨텍스트를 우회하므로 직접 +1).
                post.getViewCount() + 1,
                post.getCreatedAt(),
                isAuthor(post, viewerId)
        );
    }

    @Transactional
    public void updatePost(UUID postId, UUID requesterId, PostUpdateRequest request) {
        CommunityPost post = findActivePost(postId);
        if (!isAuthor(post, requesterId)) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }
        post.update(request.title(), request.body());
    }

    @Transactional
    public void deletePost(UUID postId, UUID requesterId) {
        CommunityPost post = findActivePost(postId);
        if (!isAuthor(post, requesterId)) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }
        post.softDelete();
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID postId) {
        findActivePost(postId);

        return communityPostCommentRepository.findActiveByPostId(postId).stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getParent() == null ? null : comment.getParent().getId(),
                        authorName(comment.getAuthor()),
                        comment.getBody(),
                        comment.getCreatedAt()))
                .toList();
    }

    @Transactional
    public CommentResponse createComment(UUID postId, UUID authorId, CommentCreateRequest request) {
        CommunityPost post = findActivePost(postId);
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        CommunityPostComment parent = null;
        if (request.parentId() != null) {
            parent = communityPostCommentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

            // 부모 댓글은 반드시 같은 게시글 소속이어야 한다(DB 복합 FK와 동일한 규칙).
            if (!parent.getPost().getId().equals(postId)) {
                throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
            }
            // 대댓글의 대댓글은 허용하지 않는다(V2 트리거 validate_community_comment_parent와 동일).
            if (parent.getParent() != null) {
                throw new BusinessException(ErrorCode.NESTED_REPLY_NOT_ALLOWED);
            }
        }

        CommunityPostComment comment = communityPostCommentRepository.save(CommunityPostComment.builder()
                .post(post)
                .author(author)
                .parent(parent)
                .body(request.body())
                .build());

        return new CommentResponse(
                comment.getId(),
                parent == null ? null : parent.getId(),
                author.getDisplayName(),
                comment.getBody(),
                comment.getCreatedAt());
    }

    // --- 내부 헬퍼 ---

    private CommunityBoard findBoard(String boardSlug) {
        return communityBoardRepository.findActiveBySlug(boardSlug)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }

    private CommunityPost findActivePost(UUID postId) {
        return communityPostRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private boolean isAuthor(CommunityPost post, UUID userId) {
        return userId != null && post.getAuthor() != null && post.getAuthor().getId().equals(userId);
    }

    /** 작성자가 탈퇴(ON DELETE SET NULL)했거나 소프트 삭제된 계정이면 익명 표기 (V1 스키마 주석). */
    private String authorName(User author) {
        if (author == null || author.getDeletedAt() != null) {
            return UNKNOWN_AUTHOR;
        }
        return author.getDisplayName();
    }
}
