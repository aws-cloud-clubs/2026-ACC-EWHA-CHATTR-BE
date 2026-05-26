package com.acc.chattr.common.exception;

import com.acc.chattr.common.code.BusinessErrorCode;
import com.acc.chattr.common.code.Code;
import com.acc.chattr.common.code.GeneralErrorCode;
import com.acc.chattr.common.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.TooManyRequestsException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 커스텀 예외 ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<Void>> handleBusinessException(BusinessException e, HttpServletRequest req) {
        BusinessErrorCode code = e.getBusinessErrorCode();
        log.warn("BusinessException: {} [{}]", code.name(), req.getRequestURI());
        return respond(code);
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<Response<Void>> handleGeneralException(GeneralException e, HttpServletRequest req) {
        GeneralErrorCode code = e.getErrorCode();
        log.warn("GeneralException: {} [{}]", code.name(), req.getRequestURI());
        return respond(code);
    }

    // ==================== 보안 ====================

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<Response<Void>> handleAccessDenied(Exception e, HttpServletRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GeneralErrorCode code = (auth == null || auth instanceof AnonymousAuthenticationToken)
                ? GeneralErrorCode.UNAUTHORIZED
                : GeneralErrorCode.FORBIDDEN;
        log.warn("AccessDenied: {} [{}]", code.name(), req.getRequestURI());
        return respond(code);
    }

    // ==================== Validation ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
            HttpServletRequest req) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed [{}] - {}", req.getRequestURI(), detail);
        return respond(GeneralErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException e, HttpServletRequest req) {
        String detail = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("ConstraintViolation [{}] - {}", req.getRequestURI(), detail);
        return respond(GeneralErrorCode.VALIDATION_ERROR);
    }

    // ==================== HTTP 요청 ====================

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Response<Void>> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest req) {
        log.warn("NoResourceFound [{}]", req.getRequestURI());
        return respond(GeneralErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e,
            HttpServletRequest req) {
        log.warn("MethodNotSupported [{}]: {}", req.getRequestURI(), e.getMethod());
        return respond(GeneralErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Response<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e,
            HttpServletRequest req) {
        log.warn("MediaTypeNotSupported [{}]", req.getRequestURI());
        return respond(GeneralErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, ConversionFailedException.class})
    public ResponseEntity<Response<Void>> handleTypeMismatch(Exception e, HttpServletRequest req) {
        log.warn("TypeMismatch [{}]: {}", req.getRequestURI(), e.getMessage());
        return respond(GeneralErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Response<Void>> handleMissingParameter(MissingServletRequestParameterException e,
            HttpServletRequest req) {
        log.warn("MissingParameter [{}]: {}", req.getRequestURI(), e.getParameterName());
        return respond(GeneralErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleMessageNotReadable(HttpMessageNotReadableException e,
            HttpServletRequest req) {
        log.warn("MessageNotReadable [{}]", req.getRequestURI());
        return respond(GeneralErrorCode.BAD_REQUEST);
    }

    // ==================== Cognito ====================

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<Response<Void>> handleNotAuthorized(NotAuthorizedException e, HttpServletRequest req) {
        log.warn("Cognito NotAuthorized [{}]", req.getRequestURI());
        return respond(GeneralErrorCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<Response<Void>> handleUsernameExists(UsernameExistsException e, HttpServletRequest req) {
        log.warn("Cognito UsernameExists [{}]", req.getRequestURI());
        return respond(GeneralErrorCode.USER_ALREADY_EXISTS);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Response<Void>> handleInvalidPassword(InvalidPasswordException e, HttpServletRequest req) {
        log.warn("Cognito InvalidPassword [{}]", req.getRequestURI());
        return respond(GeneralErrorCode.INVALID_PASSWORD);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Response<Void>> handleCognitoUserNotFound(UserNotFoundException e, HttpServletRequest req) {
        log.warn("Cognito UserNotFound [{}]", req.getRequestURI());
        return respond(GeneralErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Response<Void>> handleTooManyRequests(TooManyRequestsException e, HttpServletRequest req) {
        log.warn("Cognito TooManyRequests [{}]", req.getRequestURI());
        return ResponseEntity.status(GeneralErrorCode.TOO_MANY_REQUESTS.getStatusCode())
                .header("Retry-After", "30")
                .body(Response.fail(GeneralErrorCode.TOO_MANY_REQUESTS));
    }

    // ==================== 최종 fallback ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleException(Exception e, HttpServletRequest req) {
        log.error("Unhandled exception [{}]", req.getRequestURI(), e);
        return respond(GeneralErrorCode.INTERNAL_SERVER_ERROR);
    }

    // ==================== 헬퍼 ====================

    private ResponseEntity<Response<Void>> respond(Code code) {
        return ResponseEntity.status(code.getStatusCode()).body(Response.fail(code));
    }
}
