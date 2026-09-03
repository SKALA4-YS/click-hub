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

    // 프로젝트 - 3장 "실제로 접속 가능한 웹서비스만 허용"
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_001", "프로젝트를 찾을 수 없습니다."),
    PROJECT_URL_UNREACHABLE(HttpStatus.BAD_REQUEST, "PROJECT_002", "접속 가능한 URL이 아닙니다."),
    NOT_PROJECT_OWNER(HttpStatus.FORBIDDEN, "PROJECT_003", "프로젝트 소유자만 접근할 수 있습니다."),

    // 반응 - 7장 "사용자당 프로젝트 1회"
    ALREADY_REACTED(HttpStatus.CONFLICT, "REACTION_001", "이미 반응을 남긴 프로젝트입니다."),

    // 구독 - 7장 "제작자를 구독"
    CANNOT_SUBSCRIBE_SELF(HttpStatus.BAD_REQUEST, "SUBSCRIBE_001", "자기 자신은 구독할 수 없습니다."),

    // 알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "알림을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
