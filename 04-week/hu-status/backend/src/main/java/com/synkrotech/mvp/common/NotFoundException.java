package com.synkrotech.mvp.common;

/** The requested resource does not exist. Rendered as 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
