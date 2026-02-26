package com.example.weaver.utils;

import com.example.weaver.dtos.others.oauths.OAuth2UserInfo;
import com.example.weaver.dtos.others.oauths.OAuth2UserInfoFactory;
import com.example.weaver.enums.AuthProvider;
import com.example.weaver.enums.UserStatus;
import com.example.weaver.exceptions.ForbiddenException;
import com.example.weaver.models.User;
import com.example.weaver.services.Others.JwtService;
import com.example.weaver.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserService userService;
    private final JwtService jwtService;
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

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        String redirectUrl =
                webUrl + "?accessToken=" + accessToken
                        + "&refreshToken=" + refreshToken;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}