package com.example.weaver.repositories;

import com.example.weaver.models.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByToken(String token);

    boolean existsByUser_EmailAndExpiryDateBefore(String email, Instant time);
    void deleteByUsedTrueOrExpiryDateBefore(Instant time);
}
