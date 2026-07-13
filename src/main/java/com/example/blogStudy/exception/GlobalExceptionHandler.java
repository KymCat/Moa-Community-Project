package com.example.blogStudy.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice   // 프로젝트 전체 Controller 에서 발생하는 예외 처리 클래스
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)    // CustomException 발생 시 메서드 실행
    public ResponseEntity<ErrorResponse> handleCustomException(
            CustomException e,
            HttpServletRequest request)
    {

        ErrorCode errorCode = e.getErrorCode();
        log.warn("비지니스 예외, code={}, path={}",
                errorCode.getCode(), request.getRequestURI());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, request.getRequestURI()));
    }

    // DTO 유효성 검증 예외 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request)
    {
        log.warn("DTO 검증 예외 path = {}", request.getRequestURI());

        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        StringBuilder messages = new StringBuilder();
        for (FieldError error : errors) {
            messages.append(error.getDefaultMessage()).append("\n");
        }

        return ResponseEntity
                .status(400)
                .body(ErrorResponse.of(
                        400,
                        ErrorCode.INVALID_INPUT_VALUE.getCode(),
                        messages.toString(),
                        request.getRequestURI()));
    }

    // 쿠키 예외
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(
            MissingRequestCookieException e,
            HttpServletRequest request)
    {
        log.warn("요청 헤더 Cookie 예외 path = {}", request.getRequestURI());

        return ResponseEntity
                .status(400)
                .body(ErrorResponse.of(
                        400,
                        ErrorCode.REFRESH_NOT_FOUND.getCode(),
                        ErrorCode.REFRESH_NOT_FOUND.getMessage(),
                        request.getRequestURI()));
    }

    // Controller 개별 파라미터 검증 예외 (@Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e,
            HttpServletRequest request)
    {
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String parameterPath = violation.getPropertyPath().toString();
            Object invalidValue = violation.getInvalidValue();
            String validationMsg = violation.getMessage();

            String logs = String.format("'%s' 값 '%s' 은 유효하지 않습니다. %s",
                    parameterPath, invalidValue, validationMsg);
            log.error(logs);
        }

        return ResponseEntity
                .status(400)
                .body(ErrorResponse.of(
                        400,
                        ErrorCode.INVALID_REQUEST_VALUE.getCode(),
                        ErrorCode.INVALID_REQUEST_VALUE.getMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request)
    {
        log.error("Controller 요청값 파라미터 변환 타입 미스매치 예외 path = {}", request.getRequestURI());

        return ResponseEntity
                .status(400)
                .body(ErrorResponse.of(
                        400,
                        ErrorCode.INVALID_REQUEST_TYPE_MISMATCH.getCode(),
                        ErrorCode.INVALID_REQUEST_TYPE_MISMATCH.getMessage(),
                        request.getRequestURI()));
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleException(
//            Exception e,
//            HttpServletRequest request
//    ) {
//        log.error("처리되지 않은 예외, path={}", request.getRequestURI());
//
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ErrorResponse.of(
//                        ErrorCode.INTERNAL_SERVER_ERROR,
//                        request.getRequestURI()
//                ));
//    }
}
