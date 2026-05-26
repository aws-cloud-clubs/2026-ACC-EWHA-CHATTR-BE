package com.acc.chattr.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GeneralErrorCode implements Code {

    // ==================== 공통 ====================
    INTERNAL_SERVER_ERROR(500, "서버 에러, 관리자에게 문의 바랍니다."),
    BAD_REQUEST(400, "잘못된 요청입니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    NOT_FOUND(404, "리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(405, "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(415, "지원하지 않는 미디어 타입입니다."),

    // ==================== 입력값 검증 ====================
    VALIDATION_ERROR(400, "입력값이 올바르지 않습니다."),
    INVALID_REQUEST_PARAMETER(400, "잘못된 요청 파라미터입니다."),

    // ==================== 인증/토큰 ====================
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(401, "토큰이 만료되었습니다."),

    // ==================== Cognito 인증 ====================
    USER_NOT_CONFIRMED(400, "이메일 인증이 완료되지 않았습니다."),
    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_ALREADY_EXISTS(409, "이미 가입된 이메일입니다."),
    INVALID_VERIFICATION_CODE(400, "잘못된 인증 코드입니다."),
    VERIFICATION_CODE_EXPIRED(400, "인증 코드가 만료되었습니다."),
    INVALID_PASSWORD(400, "비밀번호가 정책을 충족하지 않습니다."),
    TOO_MANY_REQUESTS(429, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");

    private final int statusCode;
    private final String message;
}
