package com.example.weaver.exceptions;

public class BadRequestException extends AppException {
    public BadRequestException(String message) {
        super(400,"BAD_REQUEST",message);
    }
}
