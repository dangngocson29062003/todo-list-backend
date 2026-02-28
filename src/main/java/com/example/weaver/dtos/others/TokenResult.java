package com.example.weaver.dtos.others;

import java.time.Instant;

public record TokenResult(
        String accessToken,
        String refreshToken,
        Instant expiryDate
) {}