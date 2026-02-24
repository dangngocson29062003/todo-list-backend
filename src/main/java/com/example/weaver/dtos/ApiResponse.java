package com.example.weaver.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private int status;
    private String statusCode;
    private String message;
    private T data;
    private Instant timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .statusCode("SUCCESS")
                .message("OK")
                .data(data)
                .timestamp(Instant.now())
                .build();
    }
    public static ApiResponse<Void> error(
            int status,
            String statusCode,
            String message
    ){
        return ApiResponse.<Void>builder()
                .status(status)
                .statusCode(statusCode)
                .message(message)
                .data(null)
                .timestamp(Instant.now())
                .build();

    }
    public static <T> ApiResponse<T> error(
            int status,
            String statusCode,
            String message,
            T data
    ){
        return ApiResponse.<T>builder()
                .status(status)
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }
}
