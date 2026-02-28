package com.example.weaver.exceptions;

public class InvalidTokenException extends AppException {
    public InvalidTokenException() {
        super(401,"UNAUTHORIZED","Invalid Token");
    }
}
