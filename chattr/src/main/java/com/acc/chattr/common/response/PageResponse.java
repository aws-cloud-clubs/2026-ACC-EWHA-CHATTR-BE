package com.acc.chattr.common.response;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext
) {
    public static <T> PageResponse<T> of(List<T> allItems, int page, int size) {
        int total = allItems.size();
        int from = page * size;
        int to = Math.min(from + size, total);
        List<T> content = from >= total ? List.of() : allItems.subList(from, to);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PageResponse<>(content, page, size, total, totalPages, from + size < total);
    }
}
