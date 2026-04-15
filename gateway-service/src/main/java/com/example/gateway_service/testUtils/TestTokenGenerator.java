package com.example.gateway_service.testUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class TestTokenGenerator {

    public static void main(String[] args) {
        String secret = "12345678901234567890123456789012"; // min 32 chars

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject("demo-user")
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key) // NU mai pui algoritm
                .compact();

        System.out.println("JWT TOKEN:");
        System.out.println(token);
    }
}