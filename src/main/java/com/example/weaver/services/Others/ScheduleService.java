package com.example.weaver.services.Others;

import com.example.weaver.services.EmailVerificationTokenService;
import com.example.weaver.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final EmailVerificationTokenService emailService;
    private final RefreshTokenService refreshTokenService;

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
}
