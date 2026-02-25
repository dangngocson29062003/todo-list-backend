package com.example.weaver.exceptions;

public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super(403,"FORBIDDEN",message);
    }
}
