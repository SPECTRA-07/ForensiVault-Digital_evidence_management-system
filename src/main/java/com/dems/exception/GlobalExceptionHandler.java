package com.dems.exception;

import com.dems.audit.AuditService;
import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized REST exception handler providing structured error payloads and recording failed audit events across DEMS.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final AuditService auditService;

    public GlobalExceptionHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        recordAuditFailure(request, "RESOURCE_NOT_FOUND", ex.getMessage(), AuditStatus.WARNING);
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex, HttpServletRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        recordAuditFailure(request, "BAD_REQUEST", ex.getMessage(), AuditStatus.FAILED);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex, HttpServletRequest request) {
        log.warn("Conflict error: {}", ex.getMessage());
        recordAuditFailure(request, "CONFLICT", ex.getMessage(), AuditStatus.FAILED);
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, HttpServletRequest request) {
        log.warn("Unauthorized request: {}", ex.getMessage());
        recordAuditFailure(request, "UNAUTHORIZED", ex.getMessage(), AuditStatus.FAILED);
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerException(
            InternalServerException ex, HttpServletRequest request) {
        log.error("Internal server error: ", ex);
        recordAuditFailure(request, "INTERNAL_SERVER_ERROR", ex.getMessage(), AuditStatus.FAILED);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation error processing request path: {}", request.getRequestURI());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        recordAuditFailure(request, "VALIDATION_FAILED", "Validation failed for request parameters: " + errors, AuditStatus.FAILED);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed for request parameters", request.getRequestURI(), errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception encountered: ", ex);
        recordAuditFailure(request, "UNHANDLED_EXCEPTION", ex.getMessage(), AuditStatus.FAILED);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred", request.getRequestURI(), null);
    }

    private void recordAuditFailure(HttpServletRequest request, String errorCategory, String reason, AuditStatus status) {
        try {
            auditService.recordEvent(
                    AuditAction.VIEW,
                    AuditEntityType.SYSTEM,
                    "SYSTEM_EXCEPTION",
                    request.getRequestURI(),
                    null,
                    status,
                    "HTTP Request failed [" + errorCategory + "] on path: " + request.getRequestURI(),
                    reason
            );
        } catch (Exception e) {
            log.warn("Could not log exception event to audit trail", e);
        }
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status, String message, String path, Map<String, String> validationErrors) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
        return new ResponseEntity<>(response, status);
    }
}
