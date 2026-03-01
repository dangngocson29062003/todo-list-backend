package com.example.weaver.dtos.others.results;

import java.time.Instant;
import java.util.UUID;

public record RevokeValidTokenResult(UUID userId, Instant expiryDate) {
}
