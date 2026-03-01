package com.example.weaver.services;

import com.example.weaver.dtos.others.results.LocationResult;
import com.example.weaver.dtos.others.results.RevokeValidTokenResult;
import com.example.weaver.dtos.others.results.ActiveSessionsResult;
import com.example.weaver.dtos.responses.ActiveSessionResponse;
import com.example.weaver.exceptions.InvalidTokenException;
import com.example.weaver.models.RefreshToken;
import com.example.weaver.models.User;
import com.example.weaver.repositories.RefreshTokenRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

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
    public List<RefreshToken> getActiveSessions(UUID userId) {
        return refreshTokenRepository.getActiveSessions(userId);
    }

    public RevokeValidTokenResult revokeValidToken(String token, Instant expiryDate) {
        return refreshTokenRepository.revokeValidToken(token, expiryDate);
    }

    public void saveAll(List<RefreshToken> tokensToUpdate) {
        refreshTokenRepository.saveAll(tokensToUpdate);
    }
}
