package io.thoughtcode.springboot3.web.util;

import static java.util.stream.Collectors.joining;

import java.util.List;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import lombok.Getter;

@Getter
public class GenericResponse {

    private String message;

    private String error;

    public GenericResponse(final String message) {
        this(message, null);
    }

    public GenericResponse(final String message, final String error) {
        this.message = message;
        this.error = error;
    }

    public GenericResponse(final List<ObjectError> allErrors, final String error) {
        this.error = error;
        final String allErrorsJoined = allErrors.stream().map(e -> {
            if (e instanceof FieldError) {
                return "{\"field\":\"" + ((FieldError) e).getField() + "\",\"defaultMessage\":\"" + e.getDefaultMessage() + "\"}";
            } else {
                return "{\"object\":\"" + e.getObjectName() + "\",\"defaultMessage\":\"" + e.getDefaultMessage() + "\"}";
            }
        }).collect(joining(","));
        this.message = "[" + allErrorsJoined + "]";
    }
}
