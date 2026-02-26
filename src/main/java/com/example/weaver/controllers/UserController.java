package com.example.weaver.controllers;

import com.example.weaver.dtos.requests.LoginRequest;
import com.example.weaver.dtos.requests.RefreshTokenRequest;
import com.example.weaver.dtos.requests.RegisterRequest;
import com.example.weaver.dtos.responses.LoginResponse;
import com.example.weaver.models.User;
import com.example.weaver.services.AppService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final AppService appService;
    @Value("${app.web-url}")
    private String webUrl;

    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest registerRequest){
        appService.register(registerRequest.getEmail(),registerRequest.getPassword());
    }
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest){
        return appService.login(loginRequest.getEmail(),loginRequest.getPassword());
    }
    @GetMapping("/verify")
    public void verify(@RequestParam String token,
                       HttpServletResponse response) throws IOException {
        boolean success=appService.verifyEmail(token);
        response.sendRedirect(webUrl+"/email-verified?success="+success);
    }
    @PostMapping("/refresh")
    public LoginResponse refresh( @RequestBody RefreshTokenRequest request){
        //Can luu refreshToken vao db
        //refresh->invalidate refreshToken->return new access and refreshToken
        String newAccessToken= appService.getNewAccessToken(request.getRefreshToken());
        return new LoginResponse(newAccessToken,request.getRefreshToken());
    }

}
