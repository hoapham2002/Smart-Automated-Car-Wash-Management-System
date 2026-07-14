package com.autowash.autowash_pro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter @Setter
public class JwtProperties {
    private String secret;
    private long expiration;         // 86400000 = 24h
    private long refreshExpiration;  // 604800000 = 7 ngày
}