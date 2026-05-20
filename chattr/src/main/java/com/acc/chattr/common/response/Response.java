package com.acc.chattr.common.response;

import com.acc.chattr.common.code.Code;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Response<T> {

    private final boolean success;
    private final int statusCode;
    private final String message;
    private final T data;

    public static <T> Response<T> ok(T data) {
        return new Response<>(true, 200, "OK", data);
    }

    public static Response<Void> ok() {
        return new Response<>(true, 200, "OK", null);
    }

    public static Response<Void> fail(Code code) {
        return new Response<>(false, code.getStatusCode(), code.getMessage(), null);
    }
}
