package com.notification.core.exception;

public class TemplateServiceUnavailableException extends RuntimeException {

    public TemplateServiceUnavailableException(String message) {
        super(message);
    }

    public TemplateServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

