package com.example.weaver.repositories;

import com.example.weaver.dtos.others.results.RevokeValidTokenResult;
import com.example.weaver.models.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByHashedToken(String token);

    List<RefreshToken> findByUser_Id(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT rt FROM RefreshToken rt
            WHERE rt.user.id = :userId
            AND rt.revoked = false
            ORDER BY rt.createdAt ASC
            """)
    List<RefreshToken> findActiveTokensForUpdate(UUID userId);

    @Modifying
    @Query(value = """
            UPDATE refresh_tokens
            SET revoked = true, last_used_at = :now
            WHERE hashed_token = :hashedToken
              AND revoked = false
              AND expiry_date > :now
            """,
            nativeQuery = true)
    int revokeValidToken(
            @Param("hashedToken") String hashedToken,
            @Param("now") Instant now);

    @Query("""
                SELECT rt
                FROM RefreshToken rt
                WHERE rt.user.id = :userId
                  AND rt.revoked = false
                  AND rt.expiryDate > CURRENT_TIMESTAMP
            """)
    List<RefreshToken> getActiveSessions(UUID userId);

    @Modifying
    @Query("""
                UPDATE RefreshToken rt
                SET rt.revoked = true
                WHERE rt.user.id = :userId
                  AND rt.hashedToken != :hashedToken
                  AND rt.revoked = false
            """)
    void forceLogoutOtherSessions(
            @Param("userId") UUID userId,
            @Param("hashedToken") String hashedToken
    );

    void deleteByExpiryDateBefore(Instant expiryDate);

    void deleteByHashedTokenAndUserId(String hashedToken, UUID userId);
}
