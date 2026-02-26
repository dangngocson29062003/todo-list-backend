package com.example.weaver.services.Others;

import com.example.weaver.dtos.events.UserRegisteredEvent;
import com.example.weaver.dtos.events.EmailVerificationExpiredEvent;
import com.example.weaver.exceptions.AppException;
import com.example.weaver.services.EmailVerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EventListenerService {
    private final EmailVerificationTokenService emailService;


    //Use Outbox pattern in the future
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            emailService.sendVerificationEmail(event.userId(), event.email());
        } catch (Exception e) {
            throw new AppException(500, "EMAIL_FAILED", "Could not send verification email");
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true)
    public void resendVerification(EmailVerificationExpiredEvent event) {
        try {
            emailService.sendVerificationEmail(event.userId(), event.email());
        } catch (Exception e) {
            throw new AppException(500, "EMAIL_FAILED", "Could not send verification email");
        }
    }
}