package com.example.weaver.exceptions;

import com.example.weaver.dtos.others.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.error(
                        e.getStatus(),
                        e.getStatusCode(),
                        e.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex) {

        FieldError firstError = ex.getBindingResult().getFieldError();

        String message = firstError != null
                ? firstError.getDefaultMessage()
                : "Validation error";

        return ResponseEntity.badRequest().body(
                ApiResponse.error(
                        400,
                        "VALIDATION_ERROR",
//                        "Invalid request data",
                        message,
                        null
                )
        );
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(
            AccessDeniedException ex) {
        return ResponseEntity.status(403).body(
                ApiResponse.error(
                        403,
                        "ACCESS_DENIED",
                        "You do not have permission"
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnknownException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error(
                        500,
                        "INTERNAL_SERVER_ERROR",
                        "Something went wrong"
                )
        );
    }
}
