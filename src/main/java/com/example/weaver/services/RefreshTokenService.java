package com.example.weaver.services;

import com.example.weaver.dtos.others.results.RevokeValidTokenResult;
import com.example.weaver.exceptions.InvalidTokenException;
import com.example.weaver.models.RefreshToken;
import com.example.weaver.models.User;
import com.example.weaver.repositories.RefreshTokenRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final EntityManager entityManager;

    @Transactional
    public void save(String hashedToken, UUID userId, Instant expiryDate,
                             String ipAddress, String deviceInfo) {
        checkIfExceedTokenLimit(userId);
        User user=entityManager.getReference(User.class,userId);
        RefreshToken refreshToken=RefreshToken.builder()
                .hashedToken(hashedToken)
                .user(user)
                .expiryDate(expiryDate)
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private void checkIfExceedTokenLimit(UUID userId) {
        List<RefreshToken> refreshTokens=refreshTokenRepository.findActiveTokensForUpdate(userId);
        int MAX_TOKEN_PER_USER = 5;
        if(refreshTokens.size()>=MAX_TOKEN_PER_USER){
            refreshTokens.get(0).setRevoked(true);
        }
    }

    public RefreshToken findByToken(String hashedToken) {
        return refreshTokenRepository.findByHashedToken(hashedToken).orElseThrow(InvalidTokenException::new);
    }
    public List<RefreshToken> getActiveSessions(UUID userId) {
        return refreshTokenRepository.getActiveSessions(userId);
    }
    public void forceLogoutOtherSessions(UUID userId,String hashedToken) {
        refreshTokenRepository.forceLogoutOtherSessions(userId,hashedToken);
    }

    public int revokeValidToken(String hashedToken, Instant expiryDate) {
        return refreshTokenRepository.revokeValidToken(hashedToken, expiryDate);
    }

    public void saveAll(List<RefreshToken> tokensToUpdate) {
        refreshTokenRepository.saveAll(tokensToUpdate);
    }

    public void deleteByExpiryDateBefore(Instant now) {
        refreshTokenRepository.deleteByExpiryDateBefore(now);
    }
}
