package com.example.weaver.services;

import com.example.weaver.enums.UserStatus;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.EmailVerificationToken;
import com.example.weaver.models.User;
import com.example.weaver.repositories.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationTokenService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(User user) {
        String token=create(user);
        String verificationUrl =
                "http://localhost:8080/api/auth/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify your account");
        message.setText(
                "Click the link below to verify your account:\n\n"
                        + verificationUrl
                        + "\n\nThis link expires in 24 hours."
        );

        mailSender.send(message);
    }
    public String create(User user){
        String token = UUID.randomUUID().toString();

        EmailVerificationToken emailVerificationToken =
                EmailVerificationToken.builder()
                        .user(user)
                        .token(token)
                        .expiryDate(Instant.now().plus(24, ChronoUnit.HOURS))
                        .used(false)
                        .build();

        return tokenRepository.save(emailVerificationToken).getToken();
    }

    public boolean verify(String token){
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                        .orElseThrow(() -> new NotFoundException("Invalid token"));

        if (verificationToken.isUsed()) {
//            throw new BadRequestException("Token already used");
            return false;
        }

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
//            throw new BadRequestException("Token expired");
            return false;
        }

        User user = verificationToken.getUser();
        if (user.getStatus() == UserStatus.ACTIVE) {
            return true;
        }

        verificationToken.setUsed(true);
        user.setStatus(UserStatus.ACTIVE);
        return true;
    }

    public void deleteUsedOrExpired(){
        tokenRepository.deleteByUsedTrueOrExpiryDateBefore(Instant.now());
    }
}
