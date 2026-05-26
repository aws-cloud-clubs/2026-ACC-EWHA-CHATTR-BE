package com.acc.chattr.common.response;

import java.util.List;

/**
 * DynamoDB LastEvaluatedKey 기반 커서 페이지네이션 응답.
 * 클라이언트는 {@code nextCursor}가 null이 될 때까지 반복 요청하여 전체 데이터를 순회합니다.</p>
 */
public record CursorPageResponse<T>(
    List<T> content,
    int size,
    String nextCursor,
    boolean hasNext
) {
    public static <T> CursorPageResponse<T> of(List<T> content, String nextCursor) {
        return new CursorPageResponse<>(content, content.size(), nextCursor, nextCursor != null);
    }
}
