package com.example.weaver.enums;

public enum AuthProvider {
    LOCAL,
    GOOGLE;
    public static AuthProvider from(String registrationId) {
        return AuthProvider.valueOf(registrationId.toUpperCase());
    }
}