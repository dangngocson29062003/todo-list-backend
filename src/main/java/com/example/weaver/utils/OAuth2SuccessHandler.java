package com.example.weaver.utils;

import com.example.weaver.dtos.others.oauths.OAuth2UserInfo;
import com.example.weaver.dtos.others.oauths.OAuth2UserInfoFactory;
import com.example.weaver.enums.AuthProvider;
import com.example.weaver.enums.UserStatus;
import com.example.weaver.exceptions.ForbiddenException;
import com.example.weaver.models.User;
import com.example.weaver.services.AppService;
import com.example.weaver.services.Others.JwtService;
import com.example.weaver.services.RefreshTokenService;
import com.example.weaver.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AppService appService;
    @Value("${app.web-url}")
    private String webUrl;

    //Web goi den http://localhost:8080/oauth2/authorization/google de dang nhap bang google
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2AuthenticationToken token =
                (OAuth2AuthenticationToken) authentication;

        String provider = token.getAuthorizedClientRegistrationId();

        OAuth2UserInfo userInfo =
                OAuth2UserInfoFactory.get(provider,
                        token.getPrincipal().getAttributes());

        String email = userInfo.getEmail();
        String providerId = userInfo.getProviderId();

        AuthProvider providerEnum = AuthProvider.from(provider);

        User user = userService.findByEmail(email)
                .orElseGet(() ->
                        userService.createUserViaOAuth(
                                email,
                                providerEnum,
                                providerId
                        ));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new ForbiddenException("You are banned");
        }

        String refreshToken = UUID.randomUUID().toString();
        String ip = appService.extractIp(request);
        String device = request.getHeader("User-Agent");

        Instant expiryDate = Instant.now().plus(7, ChronoUnit.DAYS);
        refreshTokenService.save(appService.hashToken(refreshToken), user.getId(), expiryDate, ip, device);

        String redirectUrl = webUrl + "/oauth/success";
        appService.addRefreshTokenToCookie(refreshToken,expiryDate,response);

        //redirect to frontend then call /refresh to get access token
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

}