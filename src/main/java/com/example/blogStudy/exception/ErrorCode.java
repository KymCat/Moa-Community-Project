package com.example.blogStudy.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // User Exception
    USER_NOT_FOUND          (HttpStatus.NOT_FOUND, "USER-001", "존재하지 않는 ID 입니다."),
    DUPLICATE_USER_ID       (HttpStatus.BAD_REQUEST, "USER-002","이미 존재하는 ID 입니다."),
    INVALID_PASSWORD        (HttpStatus.UNAUTHORIZED, "USER-003", "비밀번호가 일치하지 않습니다."),
    INVALID_INPUT_VALUE     (HttpStatus.BAD_REQUEST, "USER-004", "잘못된 입력입니다."),
    SAME_AS_CURRENT_VALUE   (HttpStatus.BAD_REQUEST, "USER-005", "새로운 값이 기존과 동일한 값 입니다."),

    // Post Exception
    POST_NOT_FOUND      (HttpStatus.NOT_FOUND, "POST-001", "해당 게시글을 찾을 수 없습니다."),
    DUPLICATE_POST_ID   (HttpStatus.BAD_REQUEST, "POST-002", "이미 존재하는 게시글 ID 입니다."),
    POST_ACCESS_DENIED  (HttpStatus.FORBIDDEN, "POST-003", "해당 게시글의 대한 권한이 없습니다."),

    // Comment Exception
    COMMENT_NOT_FOUND   (HttpStatus.NOT_FOUND, "COMMENT-001", "해당 댓글을 찾을 수 없습니다."),
    COMMENT_FORBIDDEN   (HttpStatus.FORBIDDEN, "COMMENT-002", "해당 댓글에 대한 권한이 없습니다."),

    // Like Exception
    DUPLICATE_LIKE      (HttpStatus.CONFLICT, "LIKE-001", "이미 좋아요를 누른 게시글입니다."),

    // Jwt Exception
    INVALID_TOKEN       (HttpStatus.UNAUTHORIZED, "JWT-001", "유효하지 않은 토큰 입니다."),
    BLACKLISTED_TOKEN   (HttpStatus.UNAUTHORIZED, "JWT-002", "블랙 리스트에 지정된 토큰입니다."),
    EXPIRED_TOKEN       (HttpStatus.UNAUTHORIZED, "JWT-003", "이미 만료된 토큰 입니다."),
    INVALID_TOKEN_TYPE  (HttpStatus.UNAUTHORIZED, "JWT-004", "잘못된 종류의 토큰입니다."),

    // Auth Exception
    INVALID_AUTH_HEADER         (HttpStatus.UNAUTHORIZED, "AUTH-001", "유효하지 않은 헤더 입니다."),
    INVALID_TOKEN_OWNER         (HttpStatus.FORBIDDEN, "AUTH-002", "해당 토큰의 소유자가 아닙니다."),
    REFRESH_NOT_FOUND           (HttpStatus.BAD_REQUEST, "AUTH-003", "Refresh 토큰이 존재하지 않습니다"),
    AUTHENTICATION_REQUIRED     (HttpStatus.UNAUTHORIZED, "AUTH-004", "로그인이 필요합니다."),
    ACCESS_DENIED               (HttpStatus.FORBIDDEN, "AUTH-005", "접근 권한이 없습니다."),

    // Validation Exception
    INVALID_REQUEST_VALUE           (HttpStatus.BAD_REQUEST, "VALID-001", "잘못된 요청 파라미터입니다."),
    INVALID_REQUEST_TYPE_MISMATCH   (HttpStatus.BAD_REQUEST, "VALID-002", "잘못된 요청 파라미터 타입입니다."),
    INVALID_REQUEST_PARAM_MISSING   (HttpStatus.BAD_REQUEST, "VALID-003", "누락된 요청 파라미터가 있습니다."),

    // System Exception
    INTERNAL_SERVER_ERROR
            (HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다.");


    // 상수화를 위한 final
    private final HttpStatus status;
    private final String code;
    private final String message;
}



