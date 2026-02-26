package com.example.weaver.dtos.others;

import com.example.weaver.enums.EmailVerificationStatus;
import java.util.UUID;

public record EmailVerificationResult(
        EmailVerificationStatus status,
        UUID userId,
        String email
) {}