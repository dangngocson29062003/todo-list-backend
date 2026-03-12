package com.example.weaver.dtos.others.results;

import com.example.weaver.dtos.responses.UserResponse;

import java.time.Instant;

public record TokenResult(
        UserResponse userResponse,
        String accessToken,
        String refreshToken,
        Instant expiryDate
) {}