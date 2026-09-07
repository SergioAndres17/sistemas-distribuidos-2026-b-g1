package com.synkrotech.mvp.common;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Single error shape for every failing endpoint, so the frontend only has to
 * read one field ({@code message}) to show something useful.
 *
 * @param fieldErrors per-field detail, only present on validation failures
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(LocalDateTime.now(), status, error, message, null);
    }
}
