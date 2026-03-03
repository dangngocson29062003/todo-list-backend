package com.example.weaver.interceptors;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.services.Others.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;



    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Access authentication header(s) and invoke accessor.setUser(user)
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            log.info("header: {}", authHeader);

           if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                Claims claims = jwtService.parseToken(token);

                UUID userId = jwtService.getUserId(claims);
                String email = jwtService.getEmail(claims);

                AuthUser authUser = new AuthUser();
                authUser.setId(userId);
                authUser.setEmail(email);

                log.info("AuthUser: {}", authUser);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(authUser, null, null);

                accessor.setUser(() -> String.valueOf(userId));
            }
        }
        return message;
    }

}

