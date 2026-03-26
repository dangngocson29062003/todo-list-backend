package com.example.weaver.dtos.responses;

import java.time.Instant;
import java.util.List;

public record ProjectSummaryResponses(
        List<ProjectSummaryResponse> projects,
        int currentPage,
        boolean hasNext
) {}
