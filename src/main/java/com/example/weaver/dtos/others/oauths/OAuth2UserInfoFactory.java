package com.example.weaver.dtos.others.oauths;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo get(String provider,
                                     Map<String, Object> attributes) {

        return switch (provider) {
            case "google" -> new GoogleUserInfo(attributes);
            default -> throw new RuntimeException("Unsupported provider");
        };
    }
}