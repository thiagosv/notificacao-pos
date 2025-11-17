package com.notification.core.exception;

public class QuotaServiceException extends RuntimeException {

    public QuotaServiceException(String message) {
        super(message);
    }

    public QuotaServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

