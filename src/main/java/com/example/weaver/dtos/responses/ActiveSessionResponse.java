package com.example.weaver.dtos.responses;

import com.example.weaver.dtos.others.results.ActiveSessionsResult;

import java.time.Instant;

public record ActiveSessionResponse(String location, String deviceInfo, Instant lastActive) {
}
