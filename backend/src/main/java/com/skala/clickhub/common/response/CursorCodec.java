package com.skala.clickhub.common.response;

import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 커서 기반 목록 응답의 커서 인코딩/디코딩.
 *
 * 현재는 offset을 Base64로 감싼 단순 구현이다. keyset(마지막 행의 정렬키) 방식이 대용량에서
 * 더 정확하지만, 정렬 기준이 화면마다 다르고(published_at / created_at / score) 아직 데이터량이
 * 적어 offset으로 시작한다. 커서를 불투명(opaque) 문자열로 유지했기 때문에 나중에 내부 표현만
 * keyset으로 교체해도 API 계약은 그대로 둘 수 있다.
 */
public final class CursorCodec {

    private static final String PREFIX = "offset:";

    private CursorCodec() {}

    public static String encode(int nextOffset) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((PREFIX + nextOffset).getBytes(StandardCharsets.UTF_8));
    }

    public static int decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!decoded.startsWith(PREFIX)) {
                throw new IllegalArgumentException("unexpected cursor payload");
            }
            int offset = Integer.parseInt(decoded.substring(PREFIX.length()));
            if (offset < 0) {
                throw new IllegalArgumentException("negative offset");
            }
            return offset;
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
