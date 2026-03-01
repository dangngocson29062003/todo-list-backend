package com.example.weaver.dtos.others.results;

import java.time.Instant;

public record TokenResult(
        String accessToken,
        String refreshToken,
        Instant expiryDate
) {}