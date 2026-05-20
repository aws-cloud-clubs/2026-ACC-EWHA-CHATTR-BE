package com.acc.chattr.common.exception;

import com.acc.chattr.common.code.BusinessErrorCode;
import com.acc.chattr.common.code.GeneralErrorCode;
import com.acc.chattr.common.response.Response;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 커스텀 예외 ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<Void>> handleBusinessException(BusinessException e) {
        BusinessErrorCode code = e.getBusinessErrorCode();
        log.warn("BusinessException: {} - {}", code.name(), e.getMessage());
        return ResponseEntity.status(code.getStatusCode()).body(Response.fail(code));
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<Response<Void>> handleGeneralException(GeneralException e) {
        GeneralErrorCode code = e.getErrorCode();
        log.warn("GeneralException: {} - {}", code.name(), e.getMessage());
        return ResponseEntity.status(code.getStatusCode()).body(Response.fail(code));
    }

    // ==================== 보안 ====================

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<Response<Void>> handleAccessDenied(Exception e) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GeneralErrorCode code = (auth == null || auth instanceof AnonymousAuthenticationToken)
                ? GeneralErrorCode.UNAUTHORIZED
                : GeneralErrorCode.FORBIDDEN;
        log.warn("AccessDenied: {} - {}", code.name(), e.getMessage());
        return ResponseEntity.status(code.getStatusCode()).body(Response.fail(code));
    }

    // ==================== Validation ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed - [{}]", detail);
        return ResponseEntity.status(400).body(Response.fail(GeneralErrorCode.VALIDATION_ERROR));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String detail = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("ConstraintViolation - [{}]", detail);
        return ResponseEntity.status(400).body(Response.fail(GeneralErrorCode.VALIDATION_ERROR));
    }

    // ==================== HTTP 요청 ====================

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Response<Void>> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("NoResourceFound: {}", e.getMessage());
        return ResponseEntity.status(404).body(Response.fail(GeneralErrorCode.NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("MethodNotSupported: {}", e.getMessage());
        return ResponseEntity.status(405).body(Response.fail(GeneralErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Response<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("MediaTypeNotSupported: {}", e.getMessage());
        return ResponseEntity.status(415).body(Response.fail(GeneralErrorCode.UNSUPPORTED_MEDIA_TYPE));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, ConversionFailedException.class})
    public ResponseEntity<Response<Void>> handleTypeMismatch(Exception e) {
        log.warn("TypeMismatch: {}", e.getMessage());
        return ResponseEntity.status(400).body(Response.fail(GeneralErrorCode.INVALID_REQUEST_PARAMETER));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Response<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("MissingParameter: {}", e.getMessage());
        return ResponseEntity.status(400).body(Response.fail(GeneralErrorCode.INVALID_REQUEST_PARAMETER));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("MessageNotReadable: {}", e.getMessage());
        return ResponseEntity.status(400).body(Response.fail(GeneralErrorCode.BAD_REQUEST));
    }

    // ==================== 최종 fallback ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleException(Exception e) {
        log.error("Unhandled exception: ", e);
        return ResponseEntity.status(500).body(Response.fail(GeneralErrorCode.INTERNAL_SERVER_ERROR));
    }
}
