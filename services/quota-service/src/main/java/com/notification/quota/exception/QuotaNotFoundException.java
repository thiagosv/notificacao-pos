package com.notification.quota.exception;

public class QuotaNotFoundException extends RuntimeException {

    public QuotaNotFoundException(String message) {
        super(message);
    }

    public QuotaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}