package com.example.weaver.dtos.others.results;

import java.time.Instant;

public record ActiveSessionsResult(String ipAddress, String deviceInfo, Instant lastUsedAt) {
}
