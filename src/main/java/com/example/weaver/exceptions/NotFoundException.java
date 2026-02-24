package com.example.weaver.exceptions;

public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super(404,"NOT_FOUND",message);
    }
}
