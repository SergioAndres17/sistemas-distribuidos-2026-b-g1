package com.synkrotech.mvp.common;

/**
 * A rule of the domain was broken (duplicate tax id, not enough stock,
 * inactive customer...). Rendered as 409 Conflict.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
