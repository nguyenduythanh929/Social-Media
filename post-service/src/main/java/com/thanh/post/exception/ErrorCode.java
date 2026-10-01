package com.thanh.post.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    POST_NOT_FOUND(2001, "Post not found", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(2002, "Comment not found", HttpStatus.NOT_FOUND),
    CONTENT_REQUIRED(2003, "Content must not be empty", HttpStatus.BAD_REQUEST),
    CONTENT_TOO_LONG(2004, "Content is too long", HttpStatus.BAD_REQUEST),
    TOO_MANY_IMAGES(2005, "A post can have at most 4 images", HttpStatus.BAD_REQUEST),
    INVALID_MEDIA_URL(2006, "Images must be uploaded through the file service", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
