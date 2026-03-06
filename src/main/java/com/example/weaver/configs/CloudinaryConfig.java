package com.example.weaver.configs;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class CloudinaryConfig {
    @Value("${spring.cloudinary.name}")
    private String cloudinaryName;

    @Value("${spring.cloudinary.api-key}")
    private String apikey;

    @Value("${spring.cloudinary.secret-key}")
    private String secretKey;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudinaryName,
                "api_key", apikey,
                "api_secret", secretKey,
                "secure", true
        ));
    }
}
