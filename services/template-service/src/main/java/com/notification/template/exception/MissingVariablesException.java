package com.notification.template.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class MissingVariablesException extends RuntimeException {

    private final Set<String> required;
    private final Set<String> provided;

    @Override
    public String getMessage() {
        Set<String> missing = new java.util.HashSet<>(required);
        missing.removeAll(provided);
        return "Missing required variables: " + String.join(", ", missing);
    }
}

