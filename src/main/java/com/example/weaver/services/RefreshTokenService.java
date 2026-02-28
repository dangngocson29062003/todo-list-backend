package com.example.weaver.services;

import com.example.weaver.dtos.others.RevokeValidTokenResult;
import com.example.weaver.exceptions.InvalidTokenException;
import com.example.weaver.models.RefreshToken;
import com.example.weaver.models.User;
import com.example.weaver.repositories.RefreshTokenRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final EntityManager entityManager;

    public void save(String hashedToken, UUID userId, Instant expiryDate,
                             String ipAddress, String deviceInfo) {
        checkIfExceedTokenLimit(userId);
        User user=entityManager.getReference(User.class,userId);
        RefreshToken refreshToken=RefreshToken.builder()
                .token(hashedToken)
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

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElseThrow(InvalidTokenException::new);
    }

    public RevokeValidTokenResult revokeValidToken(String token, Instant expiryDate) {
        return refreshTokenRepository.revokeValidToken(token, expiryDate);
    }
}
