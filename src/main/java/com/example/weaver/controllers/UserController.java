package com.example.weaver.controllers;

import com.example.weaver.dtos.requests.LoginRequest;
import com.example.weaver.dtos.requests.RefreshTokenRequest;
import com.example.weaver.dtos.requests.RegisterRequest;
import com.example.weaver.dtos.responses.LoginResponse;
import com.example.weaver.models.User;
import com.example.weaver.services.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final AppService appService;

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest registerRequest){
        return appService.register(registerRequest.getEmail(),registerRequest.getPassword());
    }
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest){
        return appService.login(loginRequest.getEmail(),loginRequest.getPassword());
    }
    @PostMapping("/refresh")
    public LoginResponse refresh( @RequestBody RefreshTokenRequest request){
        String newAccessToken= appService.getNewAccessToken(request.getRefreshToken());
        return new LoginResponse(newAccessToken,newAccessToken);
    }

}






































