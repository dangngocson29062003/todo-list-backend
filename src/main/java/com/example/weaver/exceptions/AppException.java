package com.example.weaver.exceptions;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final int status;
    private final String statusCode;

    public AppException(int status, String statusCode, String message) {
        super(message);
        this.status = status;
        this.statusCode = statusCode;
    }
}
