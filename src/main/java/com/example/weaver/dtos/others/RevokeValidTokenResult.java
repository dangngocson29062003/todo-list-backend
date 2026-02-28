package com.example.weaver.dtos.others;

import java.time.Instant;
import java.util.UUID;

public record RevokeValidTokenResult(UUID userId, Instant expiryDate) {
}
