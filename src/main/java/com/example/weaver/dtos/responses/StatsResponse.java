package com.example.weaver.dtos.responses;

public record StatsResponse(
        long total,
        long todo,
        long inProgress,
        long review,
        long done,
        long blocked
) {}
