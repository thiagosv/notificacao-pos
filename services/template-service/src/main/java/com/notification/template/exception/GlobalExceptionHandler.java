package com.notification.template.exception;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTemplateNotFoundException(TemplateNotFoundException ex) {
        log.error("Template not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .error("TEMPLATE_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MissingVariablesException.class)
    public ResponseEntity<MissingVariablesError> handleMissingVariablesException(MissingVariablesException ex) {
        log.error("Missing variables: {}", ex.getMessage());

        MissingVariablesError error = MissingVariablesError.builder()
            .error("MISSING_VARIABLES")
            .message(ex.getMessage())
            .required(ex.getRequired())
            .provided(ex.getProvided())
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(TemplateRenderException.class)
    public ResponseEntity<ErrorResponse> handleTemplateRenderException(TemplateRenderException ex) {
        log.error("Template render error: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
            .error("TEMPLATE_RENDER_ERROR")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse error = ValidationErrorResponse.builder()
            .error("VALIDATION_ERROR")
            .message("Validation failed")
            .fieldErrors(errors)
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
            .error("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred")
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @Getter
    @Setter
    @Builder
    public static class ErrorResponse {
        private String error;
        private String message;
        private LocalDateTime timestamp;
    }

    @Getter
    @Setter
    @Builder
    public static class MissingVariablesError {
        private String error;
        private String message;
        private Set<String> required;
        private Set<String> provided;
        private LocalDateTime timestamp;
    }

    @Getter
    @Setter
    @Builder
    public static class ValidationErrorResponse {
        private String error;
        private String message;
        private Map<String, String> fieldErrors;
        private LocalDateTime timestamp;
    }
}

