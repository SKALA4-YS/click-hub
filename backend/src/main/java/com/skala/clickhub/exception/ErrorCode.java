package com.skala.clickhub.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 기획서 3장(등록/구독 정책)과 12장(인증 요구사항 열)에서 역추출한 비즈니스 예외 목록.
 * 화면 연동이 진행되며 새 케이스가 나오면 이 enum에 추가한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "요청 값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),

    // 인증/인가 - 3장 "GitHub 계정을 연결한 사용자만 등록"
    GITHUB_LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_001", "GitHub 로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않거나 만료된 토큰입니다."),
    ACCOUNT_DELETED(HttpStatus.FORBIDDEN, "AUTH_003", "삭제된 계정입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_004", "사용자를 찾을 수 없습니다."),
    ADMIN_LOGIN_DISABLED(HttpStatus.NOT_FOUND, "AUTH_005", "관리자 로그인이 활성화되지 않았습니다."),
    INVALID_ADMIN_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_006", "관리자 ID 또는 비밀번호가 올바르지 않습니다."),

    // 프로젝트 - 3장 "실제로 접속 가능한 웹서비스만 허용"
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_001", "프로젝트를 찾을 수 없습니다."),
    PROJECT_URL_UNREACHABLE(HttpStatus.BAD_REQUEST, "PROJECT_002", "접속 가능한 URL이 아닙니다."),
    NOT_PROJECT_OWNER(HttpStatus.FORBIDDEN, "PROJECT_003", "프로젝트 소유자만 접근할 수 있습니다."),
    INVALID_PROJECT_STATE(HttpStatus.CONFLICT, "PROJECT_004", "현재 상태에서는 처리할 수 없는 요청입니다."),
    PROJECT_CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "PROJECT_005", "승인하려면 대표 카테고리가 필요합니다."),

    // 카탈로그 (categories / technologies) — 등록 시 slug로 참조한다
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "CATALOG_001", "존재하지 않는 카테고리입니다."),
    TECHNOLOGY_NOT_FOUND(HttpStatus.BAD_REQUEST, "CATALOG_002", "존재하지 않는 기술 스택입니다."),

    // 반응 - 7장 "사용자당 프로젝트 1회"
    ALREADY_REACTED(HttpStatus.CONFLICT, "REACTION_001", "이미 반응을 남긴 프로젝트입니다."),

    // 구독 - 7장 "제작자를 구독"
    CANNOT_SUBSCRIBE_SELF(HttpStatus.BAD_REQUEST, "SUBSCRIBE_001", "자기 자신은 구독할 수 없습니다."),

    // 알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "알림을 찾을 수 없습니다."),

    // 인사이트 - 9.1 주간 트렌드
    INSIGHT_NOT_FOUND(HttpStatus.NOT_FOUND, "INSIGHT_001", "발행된 주간 인사이트가 없습니다."),

    // 커뮤니티 게시판
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY_001", "게시판을 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY_002", "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY_003", "댓글을 찾을 수 없습니다."),
    NOT_POST_AUTHOR(HttpStatus.FORBIDDEN, "COMMUNITY_004", "작성자만 수정하거나 삭제할 수 있습니다."),
    NESTED_REPLY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COMMUNITY_005", "대댓글에는 다시 답글을 달 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
