package com.example.weaver.services.Others;

import com.example.weaver.dtos.events.NotificationCreatedEvent;
import com.example.weaver.enums.OutboxEventTopic;
import com.example.weaver.models.OutboxEvent;
import com.example.weaver.services.EmailVerificationTokenService;
import com.example.weaver.services.OutboxEventService;
import com.example.weaver.services.RefreshTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final EmailVerificationTokenService emailService;
    private final RefreshTokenService refreshTokenService;
    private final OutboxEventService outboxEventService;

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // mỗi giờ
    public void cleanupVerificationTokens() {
        emailService.deleteUsedOrExpired();
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenService.deleteByExpiryDateBefore(Instant.now());
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processPendingEvents(){
        outboxEventService.processPendingEvents();
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void cleanupSentEvents(){
        outboxEventService.cleanupSentEvents();
    }
}
