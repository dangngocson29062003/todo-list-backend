package com.example.weaver.services;

import  com.example.weaver.dtos.others.EmailVerificationResult;
import com.example.weaver.enums.UserStatus;
import com.example.weaver.enums.EmailVerificationStatus;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.EmailVerificationToken;
import com.example.weaver.models.User;
import com.example.weaver.repositories.EmailVerificationTokenRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationTokenService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final EntityManager entityManager;
    private final JavaMailSender mailSender;

    @Value("${app.server-url}")
    private String serverUrl;

    public void sendVerificationEmail(UUID userId, String email) {
        String token = create(userId);
        String verificationUrl =
                serverUrl+"/user/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify your account");
        message.setText(
                "Click the link below to verify your account:\n\n"
                        + verificationUrl
                        + "\n\nThis link expires in 24 hours."
        );

        mailSender.send(message);
    }

    public String create(UUID userId) {
        User userRef = entityManager.getReference(User.class, userId);
        String token = UUID.randomUUID().toString();

        EmailVerificationToken emailVerificationToken =
                EmailVerificationToken.builder()
                        .user(userRef)
                        .token(token)
                        .expiryDate(Instant.now().plus(24, ChronoUnit.HOURS))
                        .used(false)
                        .build();

        return tokenRepository.save(emailVerificationToken).getToken();
    }

    public EmailVerificationResult verify(String token) {

        Optional<EmailVerificationToken> verificationToken =
                tokenRepository.findByToken(token);
        if (verificationToken.isEmpty()) {
            return new EmailVerificationResult(EmailVerificationStatus.NOT_FOUND, null, null);
        }
        User user = verificationToken.get().getUser();

        if (verificationToken.get().isUsed()) {
            return new EmailVerificationResult(EmailVerificationStatus.USED, user.getId(), user.getEmail());
        }

        if (verificationToken.get().getExpiryDate().isBefore(Instant.now())) {
            return new EmailVerificationResult(EmailVerificationStatus.EXPIRED, user.getId(), user.getEmail());
        }


        verificationToken.get().setUsed(true);
        user.setStatus(UserStatus.ACTIVE);

        return new EmailVerificationResult(EmailVerificationStatus.SUCCESS, user.getId(), user.getEmail());
    }

    public void deleteUsedOrExpired() {
        tokenRepository.deleteByUsedTrueOrExpiryDateBefore(Instant.now());
    }

    public boolean checkIfTokenExpiredByEmail(String email) {
        return tokenRepository.existsByUser_EmailAndExpiryDateBefore(email, Instant.now());
    }
}
