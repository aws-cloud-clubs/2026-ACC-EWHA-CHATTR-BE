package com.acc.chattr.common.response;

import com.acc.chattr.common.code.GeneralErrorCode;
import com.acc.chattr.common.exception.GeneralException;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext
) {
    public static <T> PageResponse<T> of(List<T> allItems, int page, int size) {
        validate(page, size);
        int total = allItems.size();
        int from = page * size;
        int to = Math.min(from + size, total);
        List<T> content = from >= total ? List.of() : allItems.subList(from, to);
        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponse<>(content, page, size, total, totalPages, from + size < total);
    }

    public static <E, T> PageResponse<T> of(List<E> allItems, int page, int size, Function<E, T> mapper) {
        validate(page, size);
        int total = allItems.size();
        int from = page * size;
        int to = Math.min(from + size, total);
        List<T> content = from >= total ? List.of() :
            allItems.subList(from, to).stream().map(mapper).toList();
        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponse<>(content, page, size, total, totalPages, from + size < total);
    }

    private static void validate(int page, int size) {
        if (page < 0 || size <= 0) throw new GeneralException(GeneralErrorCode.VALIDATION_ERROR);
    }
}
