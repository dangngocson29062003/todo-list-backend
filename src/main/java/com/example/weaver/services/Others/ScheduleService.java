package com.example.weaver.services.Others;

import com.example.weaver.services.EmailVerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final EmailVerificationTokenService emailService;

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // mỗi giờ
    public void cleanupVerificationTokens() {
        emailService.deleteUsedOrExpired();
    }
}
