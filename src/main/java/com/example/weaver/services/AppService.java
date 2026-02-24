package com.example.weaver.services;

import com.example.weaver.dtos.responses.LoginResponse;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.models.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(String email, String password){
        Optional<User> user=userService.findByEmail(email);
        if(user.isEmpty() || !passwordEncoder.matches(password,user.get().getPassword())){
            throw new BadRequestException("Invalid email or password");
        }
        String accessToken= jwtService.generateAccessToken(user.get());
        String refreshToken= jwtService.generateRefreshToken(user.get());
        return new LoginResponse(accessToken,refreshToken);
    }

    @Transactional
    public User register(String email,String password){
        String hashedPassword = passwordEncoder.encode(password);
        return userService.create(email,hashedPassword);
    }

    public String getNewAccessToken(String refreshToken){
        if(!jwtService.validateToken(refreshToken)){
            throw new BadRequestException("Invalid refresh token");
        }
        UUID userId = jwtService.getUserId(refreshToken);
        User user=userService.findById(userId);
        return jwtService.generateAccessToken(user);
    }
}
