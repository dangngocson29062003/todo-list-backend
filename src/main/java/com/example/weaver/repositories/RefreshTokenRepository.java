package com.example.weaver.repositories;

import com.example.weaver.dtos.others.results.RevokeValidTokenResult;
import com.example.weaver.dtos.others.results.ActiveSessionsResult;
import com.example.weaver.models.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

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
            WHERE token = :token
              AND revoked = false
              AND expiry_date > :now
            RETURNING user_id AS userId,
                      expiry_date AS expiryDate
            """,
            nativeQuery = true)
    RevokeValidTokenResult revokeValidToken(
            String token,
            Instant now);

    //SELECT new com.example.weaver.dtos.responses.ActiveSessionsResult(
    //                    rt.ipAddress,
    //                    rt.deviceInfo,
    //                    rt.lastUsedAt
    //                )
    @Query("""
                SELECT rt
                FROM RefreshToken rt
                WHERE rt.user.id = :userId
                  AND rt.revoked = false
                  AND rt.expiryDate > CURRENT_TIMESTAMP
            """)
    List<RefreshToken> getActiveSessions(UUID userId);


}
