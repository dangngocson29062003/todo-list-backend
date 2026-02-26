package com.example.weaver.services.Others;

import com.example.weaver.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        //15m
        long accessTokenExpiration = 15 * 60 * 1000;
        return generateToken(user, accessTokenExpiration);
    }
    public String generateRefreshToken(User user) {
        //7day
        long refreshTokenExpiration = 7 * 24 * 60 * 60 * 1000;
        return generateToken(user, refreshTokenExpiration);
    }

    public String generateToken( User user, long expiresIn ) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email",user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiresIn))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    public UUID getUserId(String token) {
        return UUID.fromString(
                parseToken(token).getSubject()
        );
    }
    public String getEmail(String token) {
        return parseToken(token).get("email").toString();
    }
    public boolean isExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

}