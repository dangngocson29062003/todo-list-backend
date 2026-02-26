package com.example.weaver.dtos.events;

import com.example.weaver.models.User;

import java.util.UUID;


public record EmailVerificationExpiredEvent(UUID userId, String email) {
}
