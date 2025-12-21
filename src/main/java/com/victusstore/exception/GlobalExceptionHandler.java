package com.victusstore.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        String traceId = MDC.get("traceId");
        logger.error("Illegal argument: {}", ex.getMessage(), ex);
        
        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .traceId(traceId)
                .build();
        
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        String traceId = MDC.get("traceId");
        logger.error("Runtime exception: {}", ex.getMessage(), ex);
        
        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("RUNTIME_ERROR")
                .message(ex.getMessage())
                .traceId(traceId)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        String traceId = MDC.get("traceId");
        Map<String, Object> details = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });
        
        logger.warn("Validation failed: {}", details);
        
        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .details(details)
                .traceId(traceId)
                .build();
        
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {
        String traceId = MDC.get("traceId");
        Map<String, Object> details = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
        
        logger.warn("Constraint violation: {}", details);
        
        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("CONSTRAINT_VIOLATION")
                .message("Constraint validation failed")
                .details(details)
                .traceId(traceId)
                .build();
        
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex) {
        String traceId = MDC.get("traceId");
        logger.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("ACCESS_DENIED")
                .message("You do not have permission to access this resource")
                .traceId(traceId)
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex) {
        String traceId = MDC.get("traceId");
        logger.warn("Bad credentials: {}", ex.getMessage());
        
        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("INVALID_CREDENTIALS")
                .message("Invalid email or password")
                .traceId(traceId)
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(StockInsufficientException.class)
    public ResponseEntity<ErrorResponse> handleStockInsufficientException(
            StockInsufficientException ex) {
        String traceId = MDC.get("traceId");
        logger.warn("Stock insufficient: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getVariantId() != null) {
            details.put("variant_id", ex.getVariantId());
        }
        if (ex.getAvailableStock() != null) {
            details.put("available_stock", ex.getAvailableStock());
        }
        if (ex.getRequestedQuantity() != null) {
            details.put("requested_quantity", ex.getRequestedQuantity());
        }
        
        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("STOCK_INSUFFICIENT")
                .message(ex.getMessage())
                .details(details)
                .traceId(traceId)
                .build();
        
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        String traceId = MDC.get("traceId");
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .traceId(traceId)
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder().error(errorDetail).build());
    }
}

