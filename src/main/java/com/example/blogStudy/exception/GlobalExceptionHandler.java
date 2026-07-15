package com.example.blogStudy.exception;

import com.example.blogStudy.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice   // 프로젝트 전체 Controller 에서 발생하는 예외 처리 클래스
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)    // CustomException 발생 시 메서드 실행
    public ResponseEntity<ApiResponse<Void>> handleCustomException(
            CustomException e,
            HttpServletRequest request)
    {

        ErrorCode errorCode = e.getErrorCode();
        HttpStatus status = errorCode.getStatus();
        String code = errorCode.getCode();
        String msg = errorCode.getMessage();

        log.warn("비지니스 예외, code={}, path={}",
                errorCode.getCode(), request.getRequestURI());

        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(code, msg));
    }

    // DTO 유효성 검증 예외 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request)
    {
        log.warn("DTO 검증 예외 path = {}", request.getRequestURI());

        String code = ErrorCode.INVALID_INPUT_VALUE.getCode();
        String msg = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("\n"));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(code, msg));
    }

    // 쿠키 예외
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestCookieException(
            MissingRequestCookieException e,
            HttpServletRequest request)
    {
        log.warn("요청 헤더 Cookie 예외 path = {}", request.getRequestURI());

        String code = ErrorCode.REFRESH_NOT_FOUND.getCode();
        String msg = ErrorCode.REFRESH_NOT_FOUND.getMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(code, msg));
    }

    // Controller 개별 파라미터 검증 예외 (@Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException e)
    {
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String parameterPath = violation.getPropertyPath().toString();
            String validationMsg = violation.getMessage();

            String logs = String.format("'%s' 값이 유효하지 않습니다. %s",
                    parameterPath, validationMsg);
            log.warn(logs);
        }

        String code = ErrorCode.INVALID_REQUEST_VALUE.getCode();
        String msg = ErrorCode.INVALID_REQUEST_VALUE.getMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(code, msg));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e)
    {
        MethodParameter parameter = e.getParameter();
        String paramName = parameter.getParameterName();

        String logs = String.format("'%s' 파라미터 요청값과 타입이 불일치합니다.", paramName);
        log.warn(logs);

        String code = ErrorCode.INVALID_REQUEST_TYPE_MISMATCH.getCode();
        String msg = ErrorCode.INVALID_REQUEST_TYPE_MISMATCH.getMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(code, msg));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e)
    {
        String paramName = e.getParameterName();
        String logs = String.format("'%s' 매개변수가 누락되었습니다.",paramName);
        log.warn(logs);

        String code = ErrorCode.INVALID_REQUEST_PARAM_MISSING.getCode();
        String msg = ErrorCode.INVALID_REQUEST_PARAM_MISSING.getMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(code, msg));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("처리되지 않은 예외, path={}",
                request.getRequestURI(), e);

        String code = ErrorCode.INTERNAL_SERVER_ERROR.getCode();
        String msg = ErrorCode.INTERNAL_SERVER_ERROR.getMessage();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(code, msg));
    }
}
