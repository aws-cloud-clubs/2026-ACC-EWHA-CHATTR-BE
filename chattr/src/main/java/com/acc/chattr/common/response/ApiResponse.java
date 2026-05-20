package com.acc.chattr.common.response;

import com.acc.chattr.common.code.Code;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final boolean success;
    private final int statusCode;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, 200, "OK", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, 200, "OK", null);
    }

    public static ApiResponse<Void> fail(Code code) {
        return new ApiResponse<>(false, code.getStatusCode(), code.getMessage(), null);
    }
}
