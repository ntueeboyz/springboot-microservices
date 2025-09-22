package com.example.employee.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandling {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                      HttpServletRequest req) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", Optional.of(fe.getField()).orElse(""),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value")
                ))
                .collect(Collectors.toList());

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        applyCommon(pd, req, URI.create("about:blank/validation-error"), "Bad Request");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler({ BindException.class, ConstraintViolationException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleBindingOrConstraint(Exception ex, HttpServletRequest req) {
        List<Map<String, String>> errors = new ArrayList<>();
        if (ex instanceof BindException be) {
            for (FieldError fe : be.getFieldErrors()) {
                errors.add(Map.of(
                        "field", Optional.of(fe.getField()).orElse(""),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value")
                ));
            }
        } else if (ex instanceof ConstraintViolationException cve) {
            for (ConstraintViolation<?> v : cve.getConstraintViolations()) {
                errors.add(Map.of(
                        "field", Optional.ofNullable(v.getPropertyPath()).map(Object::toString).orElse(""),
                        "message", Optional.ofNullable(v.getMessage()).orElse("Invalid value")
                ));
            }
        }

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        applyCommon(pd, req, URI.create("about:blank/validation-error"), "Bad Request");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler({ EntityNotFoundException.class, NoSuchElementException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNotFound(RuntimeException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                Optional.ofNullable(ex.getMessage()).orElse("Resource not found"));
        applyCommon(pd, req, URI.create("about:blank/not-found"), "Not Found");
        return pd;
    }

    @ExceptionHandler({ IllegalArgumentException.class, DuplicateResourceException.class })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleBusiness(RuntimeException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                Optional.ofNullable(ex.getMessage()).orElse("Business rule violation"));
        applyCommon(pd, req, URI.create("about:blank/conflict"), "Conflict");
        return pd;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleUnknown(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
        applyCommon(pd, req, URI.create("about:blank/internal-error"), "Internal Server Error");
        return pd;
    }

    private static void applyCommon(ProblemDetail pd,
                                    HttpServletRequest req,
                                    URI type,
                                    String title) {
        pd.setType(type);
        pd.setTitle(title);
        pd.setProperty("instance", req.getRequestURI());
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("traceId", Optional.ofNullable(MDC.get("traceId"))
                .orElseGet(() -> UUID.randomUUID().toString()));
    }

    public static class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String message) { super(message); }
    }
}
