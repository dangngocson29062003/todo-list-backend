package com.example.weaver.utils;

import com.example.weaver.dtos.others.AuthUser;
import com.example.weaver.services.Others.JwtService;
import com.example.weaver.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authToken = request.getHeader("Authorization");
        if(authToken==null || !authToken.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        String token = authToken.substring(7);

        if(!jwtService.validateToken(token)){
            filterChain.doFilter(request,response);
            return;
        }
        UUID userId = jwtService.getUserId(token);
        String email = jwtService.getEmail(token);

        AuthUser authUser=new AuthUser();
        authUser.setId(userId);
        authUser.setEmail(email);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser,null,null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request,response);
    }
}
