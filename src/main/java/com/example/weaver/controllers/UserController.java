package com.example.weaver.controllers;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.dtos.others.results.EmailVerificationResult;
import com.example.weaver.dtos.others.results.TokenResult;
import com.example.weaver.dtos.requests.LoginRequest;
import com.example.weaver.dtos.requests.RegisterRequest;
import com.example.weaver.dtos.responses.ActiveSessionResponse;
import com.example.weaver.dtos.responses.LoginResponse;
import com.example.weaver.services.AppService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final AppService appService;
    @Value("${app.web-url}")
    private String webUrl;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest registerRequest) {
        appService.register(registerRequest.getEmail(), registerRequest.getPassword());
        return "Confirmation email has been sent to your email address!";
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        TokenResult tokenResult = appService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                loginRequest.getRememberMe(),
                request);

        AppService.addRefreshTokenToCookie(tokenResult.refreshToken(),
                tokenResult.expiryDate(), response);

        return new LoginResponse(tokenResult.accessToken(), tokenResult.refreshToken());
    }

    @PostMapping("/logout")
    public void logout(@CookieValue(value = "refreshToken",required = false) String refreshToken,
                       @AuthenticationPrincipal AuthUser authUser) {
        appService.logout(refreshToken,authUser.getId());
    }

    @GetMapping("/verify")
    public void verify(@RequestParam String token,
                       HttpServletResponse response) throws IOException {
        EmailVerificationResult result = appService.verifyEmail(token);

        //If status=EXPIRED->show 'New confirmation email has been sent'
        //   status=SUCCESS->show 'Account has been verified, please loggin'
        //   status=USED-> show 'Account already verified'
        //   status=NOT_FOUND-> show 'Invalid token'
        response.sendRedirect(webUrl + "/email-verified?status=" + result.status());
    }

    @PostMapping("/refresh")
    public String refresh(@CookieValue(value = "refreshToken",required = false) String refreshToken,
                          HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        TokenResult tokenResult= appService.getNewAccessToken(refreshToken, request);

        AppService.addRefreshTokenToCookie(tokenResult.refreshToken(),
                tokenResult.expiryDate(), response);

        return tokenResult.accessToken();
    }
    @GetMapping("/sessions")
    public List<ActiveSessionResponse> getActiveSessions(@AuthenticationPrincipal AuthUser authUser) {
        return appService.getActiveSession(authUser.getId());
    }
    @PostMapping("/force-logout")
    public void forceLogoutOtherSessions(@AuthenticationPrincipal AuthUser authUser,
                            @CookieValue(value = "refreshToken",required = false) String refreshToken) {
        appService.forceLogoutOtherSessions(authUser.getId(),refreshToken);
    }

}






































