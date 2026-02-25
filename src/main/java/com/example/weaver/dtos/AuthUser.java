package com.example.weaver.dtos;

import com.example.weaver.models.User;
import lombok.Data;

import java.util.UUID;

@Data
public class AuthUser{

    private UUID id;
    private String email;
//    private Collection<? extends GrantedAuthority> authorities;

    private AuthUser toAuthUser(User user) {
        AuthUser authUser = new AuthUser();
        authUser.setId(user.getId());
        authUser.setEmail(user.getEmail());
        return authUser;
    }
}