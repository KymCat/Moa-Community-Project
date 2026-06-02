package com.example.blogStudy.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
//        String message = e.getBindingResult()
//                .getFieldErrors()
//                .get(0)
//                .getDefaultMessage();

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
            HttpServletRequest request
    )
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
