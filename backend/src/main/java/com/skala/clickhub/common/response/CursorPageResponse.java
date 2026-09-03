package com.skala.clickhub.common.response;

import java.util.List;

/**
 * 커서 기반 목록 응답 공통 포맷 (Feed, Search 등 무한 스크롤 화면 공용).
 */
public record CursorPageResponse<T>(
        List<T> items,
        String nextCursor,
        boolean hasNext
) {
    public static <T> CursorPageResponse<T> of(List<T> items, String nextCursor) {
        return new CursorPageResponse<>(items, nextCursor, nextCursor != null);
    }
}
