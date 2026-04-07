package com.example.gateway_service.testUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class TestTokenGenerator {
    public static void main(String[] args) {
        String secret = "12345678901234567890123456789012";
        Key key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());

        String token = Jwts.builder()
                .setSubject("demo-user")
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println(token);
    }
}

//648a445c-656f-4422-8fad-3e7fcd67a565